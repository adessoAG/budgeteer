
ALTER TABLE BUDGETEER_USER
    ADD(GLOBAL_ROLE NUMBER(10,0) DEFAULT(0) NOT NULL ENABLE,
        ROLES RAW(1024));

CREATE TABLE CONTRACT_SORTING(
    ID NUMBER(19,0) NOT NULL ENABLE,
	SORTING_INDEX NUMBER(10,0),
	CONTRACT_ID NUMBER(19,0) NOT NULL ENABLE,
	USER_ID NUMBER(19,0) NOT NULL ENABLE,
	PRIMARY KEY (ID)
	);

CREATE SEQUENCE SEQ_CONTRACT_SORTING_ID
 START WITH     1
 INCREMENT BY   1
 NOCYCLE;

CREATE TABLE MANUAL_RECORD_ENTITY
(
  ID NUMBER(19,0) NOT NULL ENABLE,
  BILLING_DATE DATE NOT NULL ENABLE,
  CREATION_DATE DATE NOT NULL ENABLE,
  RECORD_DAY NUMBER(10,0) NOT NULL ENABLE,
  DESCRIPTION VARCHAR2(255 CHAR) NOT NULL ENABLE,
  MONEY_AMOUNT NUMBER(19,0) NOT NULL ENABLE,
  RECORD_MONTH NUMBER(10,0) NOT NULL ENABLE,
  RECORD_WEEK NUMBER(10,0) NOT NULL ENABLE,
  RECORD_YEAR NUMBER(10,0) NOT NULL ENABLE,
  BUDGET_ID NUMBER(19,0) NOT NULL ENABLE,
  PRIMARY KEY (ID)
)