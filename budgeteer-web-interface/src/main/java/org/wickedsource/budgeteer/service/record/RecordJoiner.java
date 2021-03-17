package org.wickedsource.budgeteer.service.record;

import org.springframework.stereotype.Component;
import org.wickedsource.budgeteer.MoneyUtil;
import org.wickedsource.budgeteer.persistence.record.*;
import org.wickedsource.budgeteer.service.statistics.MonthlyStats;

import java.util.*;

@Component
public class RecordJoiner {

    /**
     * Joins the given work records and the given plan records since it is not possible to join them in the database with JPA.
     */
    public List<AggregatedRecord> joinWeekly(List<WeeklyAggregatedRecordBean> workRecords, List<WeeklyAggregatedRecordBean> planRecords) {
        RecordMap recordMap = new RecordMap();
        for (WeeklyAggregatedRecordBean bean : planRecords) {
            AggregatedRecord record = recordMap.getOrCreateRecord(bean);
            record.setBudgetPlanned_net(MoneyUtil.createMoneyFromCents(bean.getValueInCents()));
        }
        for (WeeklyAggregatedRecordBean bean : workRecords) {
            AggregatedRecord record = recordMap.getOrCreateRecord(bean);
            record.setBudgetBurned_net(MoneyUtil.createMoneyFromCents(bean.getValueInCents()));
            record.setHours(bean.getHours());
        }
        List<AggregatedRecord> records = new ArrayList<AggregatedRecord>(recordMap.values());
        Collections.sort(records, new AggregatedRecordComparator());
        return records;
    }

    /**
     * Join the weekly records by calculating the money values as fraction of monthly records.
     *
     * @param workRecords  the weekly work records
     * @param planRecords  the weekly plan records
     * @param monthlyStats the monthly records
     * @return the joined week records as a List of AggregatedRecord
     */
    public List<AggregatedRecord> joinWeeklyByMonthFraction(List<WeeklyAggregatedRecordWithTitleAndTaxBean> workRecords, List<WeeklyAggregatedRecordWithTaxBean> planRecords, MonthlyStats monthlyStats) {
        List<WeeklyAggregatedRecordWithTaxBean> planList = ListJoiner.joinPlanBeanHours(planRecords);
        List<WeeklyAggregatedRecordWithTitleAndTaxBean> workList = ListJoiner.joinWorkBeanHours(workRecords);

        monthlyStats.calculateCentValuesByMonthlyFraction(planList, workList);
        List<PlanAndWorkRecord> records = ListJoiner.joinWeeklyToPlanAndWorkRecords(planList, workList);

        // Cast to AggregatedRecord
        List<AggregatedRecord> recordList = new ArrayList<>();
        for (PlanAndWorkRecord planAndWorkRecord : records) {
            recordList.add(new AggregatedRecord(planAndWorkRecord));
        }

        Collections.sort(recordList, new AggregatedRecordComparator());
        recordList = sumRecordsForEachPeriod(recordList);

        return recordList;
    }

    /**
     * Goes through a list of records by time periods and sums up the money of all records for each period.
     *
     * @param records a list of records to sum up
     * @return a list of the summed records
     */
    private List<AggregatedRecord> sumRecordsForEachPeriod(List<AggregatedRecord> records) {
        int i = 0;
        while (i < records.size() - 1) {
            AggregatedRecord current = records.get(i);
            current.removeNullValues();
            AggregatedRecord next = records.get(i + 1);
            next.removeNullValues();

            // Create new record as a sum
            if (current.getAggregationPeriodStart().equals(next.getAggregationPeriodStart()) && current.getAggregationPeriodEnd().equals(next.getAggregationPeriodEnd())) {
                AggregatedRecord sum = new AggregatedRecord();
                sum.setAggregationPeriodTitle(current.getAggregationPeriodTitle());
                sum.setAggregationPeriodStart(current.getAggregationPeriodStart());
                sum.setAggregationPeriodEnd(current.getAggregationPeriodEnd());
                sum.setHours(current.getHours() + next.getHours());

                sum.setBudgetPlanned_net(MoneyUtil.sumMoney(current.getBudgetPlanned_net(), next.getBudgetPlanned_net()));
                sum.setBudgetPlanned_gross(MoneyUtil.sumMoney(current.getBudgetPlanned_gross(), next.getBudgetPlanned_gross()));
                sum.setBudgetBurned_net(MoneyUtil.sumMoney(current.getBudgetBurned_net(), next.getBudgetBurned_net()));
                sum.setBudgetBurned_gross(MoneyUtil.sumMoney(current.getBudgetBurned_gross(), next.getBudgetBurned_gross()));

                // Replace the two old records by their sum
                records.remove(current);
                records.remove(next);
                records.add(i, sum);
            } else {
                // Go to the next record
                i++;
            }
        }

        return records;
    }

    public List<AggregatedRecord> joinMonthly(List<MonthlyAggregatedRecordBean> workRecords, List<MonthlyAggregatedRecordBean> planRecords) {
        RecordMap recordMap = new RecordMap();
        for (MonthlyAggregatedRecordBean bean : planRecords) {
            AggregatedRecord record = recordMap.getOrCreateRecord(bean);
            record.setBudgetPlanned_net(MoneyUtil.createMoneyFromCents(bean.getValueInCents()));
        }
        for (MonthlyAggregatedRecordBean bean : workRecords) {
            AggregatedRecord record = recordMap.getOrCreateRecord(bean);
            record.setBudgetBurned_net(MoneyUtil.createMoneyFromCents(bean.getValueInCents()));
            record.setHours(bean.getHours());
        }
        List<AggregatedRecord> records = new ArrayList<AggregatedRecord>(recordMap.values());
        Collections.sort(records, new AggregatedRecordComparator());
        return records;
    }

    public List<AggregatedRecord> joinMonthlyWithTax(List<MonthlyAggregatedRecordWithTaxBean> workRecords, List<MonthlyAggregatedRecordWithTaxBean> planRecords) {
        List<PlanAndWorkRecord> planAndWorkRecordList = ListJoiner.joinMonthlyToPlanAndWorkRecords(planRecords, workRecords);

        // Cast to AggregatedRecord
        List<AggregatedRecord> recordList = new ArrayList<>();
        for (PlanAndWorkRecord planAndWorkRecord : planAndWorkRecordList) {
            recordList.add(new AggregatedRecord(planAndWorkRecord));
        }

        Collections.sort(recordList, new AggregatedRecordComparator());
        recordList = sumRecordsForEachPeriod(recordList);

        return recordList;
    }

    class RecordMap extends HashMap<String, AggregatedRecord> {

        public AggregatedRecord getOrCreateRecord(WeeklyAggregatedRecordBean recordBean) {
            AggregatedRecord record = get(getKey(recordBean));
            if (record == null) {
                Calendar c = Calendar.getInstance(Locale.GERMANY);
                c.clear();
                c.set(Calendar.YEAR, recordBean.getYear());
                c.set(Calendar.WEEK_OF_YEAR, recordBean.getWeek());
                record = new AggregatedRecord();
                record.setAggregationPeriodStart(c.getTime());
                c.add(Calendar.DAY_OF_YEAR, 6);
                record.setAggregationPeriodEnd(c.getTime());
                record.setAggregationPeriodTitle(String.format("Week %d-%d", recordBean.getYear(), recordBean.getWeek()));
                put(getKey(recordBean), record);
            }
            return record;
        }

        public AggregatedRecord getOrCreateRecord(MonthlyAggregatedRecordBean recordBean) {
            AggregatedRecord record = get(getKey(recordBean));
            if (record == null) {
                Calendar c = Calendar.getInstance(Locale.GERMANY);
                c.clear();
                c.set(Calendar.YEAR, recordBean.getYear());
                c.set(Calendar.MONTH, recordBean.getMonth());
                record = new AggregatedRecord();
                record.setAggregationPeriodStart(c.getTime());
                c.add(Calendar.MONTH, 1);
                c.add(Calendar.DAY_OF_YEAR, -1);
                record.setAggregationPeriodEnd(c.getTime());
                record.setAggregationPeriodTitle(String.format("Month %d-%02d", recordBean.getYear(), recordBean.getMonth() + 1));
                put(getKey(recordBean), record);
            }
            return record;
        }

        private String getKey(WeeklyAggregatedRecordBean recordBean) {
            return String.format("%d%d", recordBean.getYear(), recordBean.getWeek());
        }

        private String getKey(MonthlyAggregatedRecordBean recordBean) {
            return String.format("%d%d", recordBean.getYear(), recordBean.getMonth());
        }
    }
}
