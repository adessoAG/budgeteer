package org.wickedsource.budgeteer.service.user;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.wickedsource.budgeteer.persistence.project.ProjectEntity;
import org.wickedsource.budgeteer.persistence.project.ProjectRepository;
import org.wickedsource.budgeteer.persistence.user.*;
import org.wickedsource.budgeteer.service.ServiceTestTemplate;
import org.wickedsource.budgeteer.service.UnknownEntityException;

import java.util.*;

import static org.mockito.Mockito.*;

@TestPropertySource(locations="classpath:application.properties")
@EnableAutoConfiguration
class UserServiceTest extends ServiceTestTemplate{

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private VerificationTokenRepository verificationTokenRepository;

    @Autowired
    private ForgotPasswordTokenRepository forgotPasswordTokenRepository;

    @Autowired
    private UserService service;

    @Autowired
    private PasswordHasher passwordHasher;


    @Test
    void testRegisterUser() throws Exception{
        service.registerUser("User", "", "Password");
        verify(userRepository, times(1)).save(any(UserEntity.class));
    }

    @Test
    void testDuplicateUsernameDuringRegistration() {
        Assertions.assertThrows(UsernameAlreadyInUseException.class, () -> {
            when(userRepository.findByName("User")).thenReturn(null, new UserEntity());
            service.registerUser("User", "", "Password");
            service.registerUser("User", "", "Password");
        });
    }

    @Test
    void testLoginSuccess() throws Exception {
        when(userRepository.findByNameOrMailAndPassword("user", passwordHasher.hash("password"))).thenReturn(createUserEntity());
        User user = service.login("user", "password");
        Assertions.assertNotNull(user);
    }

    @Test
    void testLoginFail() {
        Assertions.assertThrows(InvalidLoginCredentialsException.class, () -> {
            when(userRepository.findByNameAndPassword("user", passwordHasher.hash("password"))).thenReturn(null);
            service.login("user", "password");
        });
    }

    @Test
    void testAddUserToProjectSuccess() {
        when(userRepository.findById(1L)).thenReturn(createUserEntityOptional());
        when(projectRepository.findById(1L)).thenReturn(Optional.of(createProjectEntity()));
        service.addUserToProject(1L, 1L);
        // assertion not possible when mocking repository
    }

    @Test
    void testAddUserToProjectFailProjectNotFound() {
        Assertions.assertThrows(UnknownEntityException.class, () -> {
            when(userRepository.findById(1L)).thenReturn(createUserEntityOptional());
            service.addUserToProject(1L, 1L);
        });
    }

    @Test
    void testAddUserToProjectFailUserNotFound() {
        Assertions.assertThrows(UnknownEntityException.class, () -> {
            when(projectRepository.findById(1L)).thenReturn(Optional.of(createProjectEntity()));
            service.addUserToProject(1L, 1L);
        });
    }

    @Test
    void testRemoveUserFromProjectSuccess() {
        when(userRepository.findById(1L)).thenReturn(createUserEntityOptional());
        when(projectRepository.findById(1L)).thenReturn(Optional.of(createProjectEntity()));
        service.removeUserFromProject(1L, 1L);
        // assertion not possible when mocking repository
    }

    @Test
    void testRemoveUserFromProjectFailProjectNotFound() {
        Assertions.assertThrows(UnknownEntityException.class, () -> {
            when(userRepository.findById(1L)).thenReturn(createUserEntityOptional());
            service.removeUserFromProject(1L, 1L);
        });
    }

    @Test
    void testRemoveUserFromProjectFailUserNotFound() {
        Assertions.assertThrows(UnknownEntityException.class, () -> {
            when(projectRepository.findById(1L)).thenReturn(Optional.of(createProjectEntity()));
            service.removeUserFromProject(1L, 1L);
        });
    }

    @Test
    void testGetUsersNotInProject() {
        when(userRepository.findNotInProject(1L)).thenReturn(Arrays.asList(createUserEntity()));
        List<User> users = service.getUsersNotInProject(1L);
        Assertions.assertEquals(1, users.size());
        Assertions.assertEquals("user", users.get(0).getName());
    }

    @Test
    void testGetUsersInProject() {
        when(userRepository.findInProject(1L)).thenReturn(Arrays.asList(createUserEntity()));
        List<User> users = service.getUsersInProject(1L);
        Assertions.assertEquals(1, users.size());
        Assertions.assertEquals("user", users.get(0).getName());
    }

    @Test
    void testCheckPassword() {
        when(userRepository.findById(1L)).thenReturn(createUserEntityOptional());
        Assertions.assertTrue(service.checkPassword(1L, "password"));
        Assertions.assertFalse(service.checkPassword(1L, "PASSWORD"));
    }

    @Test
    void testResetPasswordMailNotFoundException() {
        Assertions.assertThrows(MailNotFoundException.class, () -> {
            when(userRepository.findByMail("nonuser@budgeteer.local")).thenReturn(createUserEntity());
            service.resetPassword("user@budgeteer.local");
        });
    }

    @Test
    void testResetPasswordMailNotVerifiedException() {
        Assertions.assertThrows(MailNotVerifiedException.class, () -> {
            UserEntity user = createUserEntity();
            user.setMailVerified(false);
            when(userRepository.findByMail("user@budgeteer.local")).thenReturn(user);
            service.resetPassword("user@budgeteer.local");
        });
    }

    @Test
    void testLoadUserToEdit() {
        Optional<UserEntity> userMockOptional = createUserEntityOptional();
        when(userRepository.findById(1L)).thenReturn(userMockOptional);
        EditUserData user = service.loadUserToEdit(1L);
        UserEntity userMock = userMockOptional.get();
        Assertions.assertEquals(userMock.getId(), user.getId());
        Assertions.assertEquals(userMock.getMail(), user.getMail());
        Assertions.assertEquals(userMock.getName(), user.getName());
        Assertions.assertEquals(userMock.getPassword(), user.getPassword());
    }

    @Test
    void testSaveUserUsernameAlreadyInUseException() {
        Assertions.assertThrows(UsernameAlreadyInUseException.class, () -> {
            Optional<UserEntity> user = createUserEntityOptional();
            UserEntity user2 = createUserEntity();
            user2.setId(2L);
            user2.setName("user2");
            when(userRepository.findById(1L)).thenReturn(user);
            EditUserData editUserData = service.loadUserToEdit(1L);
            when(userRepository.findByName("user2")).thenReturn(user2);
            editUserData.setName("user2");
            service.saveUser(editUserData, false);
        });
    }

    @Test
    void testSaveUserMailAlreadyInUseException() {
        Assertions.assertThrows(MailAlreadyInUseException.class, () -> {
            Optional<UserEntity> user = createUserEntityOptional();
            UserEntity user2 = createUserEntity();
            user2.setId(2L);
            user2.setMail("user2@budgeteer.local");
            when(userRepository.findById(1L)).thenReturn(user);
            EditUserData editUserData = service.loadUserToEdit(1L);
            when(userRepository.findByMail("user2@budgeteer.local")).thenReturn(user2);
            editUserData.setMail("user2@budgeteer.local");
            service.saveUser(editUserData, false);
        });
    }

    @Test
    void testSaveUser() throws MailAlreadyInUseException, UsernameAlreadyInUseException {
        Optional<UserEntity> user = createUserEntityOptional();
        when(userRepository.findById(1L)).thenReturn(user);
        EditUserData editUserData = service.loadUserToEdit(1L);
        editUserData.setName("user2");
        service.saveUser(editUserData, false);
        when(userRepository.findById(1L)).thenReturn(user);
        Assertions.assertEquals(editUserData.getName(), user.get().getName());
    }

    @Test
    void testCreateVerificationTokenForUser() {
        UserEntity user = createUserEntity();
        String uuid = UUID.randomUUID().toString();
        VerificationToken verificationToken = service.createVerificationTokenForUser(user, uuid);
        verify(verificationTokenRepository, times(1)).save(verificationToken);
    }

    @Test
    void testValidateVerificationTokenInvalid() {
        String uuid = UUID.randomUUID().toString();
        Assertions.assertEquals(-1, service.validateVerificationToken(uuid));
    }

    @Test
    void testValidateVerificationTokenExpired() {
        UserEntity user = createUserEntity();
        String uuid = UUID.randomUUID().toString();
        VerificationToken verificationToken = new VerificationToken(user, uuid);
        Date date = new Date();
        date.setTime(verificationToken.getExpiryDate().getTime() - 90000000); // 25 hours
        verificationToken.setExpiryDate(date);
        when(verificationTokenRepository.findByToken(uuid)).thenReturn(verificationToken);
        Assertions.assertEquals(-2, service.validateVerificationToken(uuid));
    }

    @Test
    void testValidateVerificationTokenValid() {
        UserEntity user = createUserEntity();
        String uuid = UUID.randomUUID().toString();
        VerificationToken verificationToken = new VerificationToken(user, uuid);
        when(verificationTokenRepository.findByToken(uuid)).thenReturn(verificationToken);
        Assertions.assertEquals(0, service.validateVerificationToken(uuid));
    }

    @Test
    void getUserByMailMailNotFoundException() {
        Assertions.assertThrows(MailNotFoundException.class, () -> {
            when(userRepository.findByMail("nonuser@budgeteer.local")).thenReturn(createUserEntity());
            service.getUserByMail("user@budgeteer.local");
        });
    }

    @Test
    void getUserByMail() throws MailNotFoundException {
        when(userRepository.findByMail("user@budgeteer.local")).thenReturn(createUserEntity());
        UserEntity user = service.getUserByMail("user@budgeteer.local");
        Assertions.assertNotNull(user);
    }

    @Test
    void getUserByIdUserIdNotFoundException() {
        Assertions.assertThrows(UserIdNotFoundException.class, () -> {
            when(userRepository.findById(2L)).thenReturn(createUserEntityOptional());
            service.getUserById(1L);
        });
    }

    @Test
    void getUserById() throws UserIdNotFoundException {
        when(userRepository.findById(1L)).thenReturn(createUserEntityOptional());
        UserEntity user = service.getUserById(1L);
        Assertions.assertNotNull(user);
    }

    @Test
    void testCreateForgotPasswordTokenForUserWithOldToken() {
        UserEntity user = createUserEntity();
        String uuid = UUID.randomUUID().toString();
        ForgotPasswordToken oldForgotPasswordToken = new ForgotPasswordToken(user, uuid);
        when(forgotPasswordTokenRepository.findByUser(user)).thenReturn(oldForgotPasswordToken);
        ForgotPasswordToken newForgotPasswordToken = service.createForgotPasswordTokenForUser(user, uuid);
        verify(forgotPasswordTokenRepository, times(1)).delete(oldForgotPasswordToken);
        verify(forgotPasswordTokenRepository, times(1)).save(newForgotPasswordToken);
    }

    @Test
    void testValidateForgotPasswordTokenInvalid() {
        String uuid = UUID.randomUUID().toString();
        Assertions.assertEquals(-1, service.validateForgotPasswordToken(uuid));
    }

    @Test
    void testValidateForgotPasswordTokenExpired() {
        UserEntity user = createUserEntity();
        String uuid = UUID.randomUUID().toString();
        ForgotPasswordToken forgotPasswordToken = new ForgotPasswordToken(user, uuid);
        Date date = new Date();
        date.setTime(forgotPasswordToken.getExpiryDate().getTime() - 90000000); // 25 hours
        forgotPasswordToken.setExpiryDate(date);
        when(forgotPasswordTokenRepository.findByToken(uuid)).thenReturn(forgotPasswordToken);
        Assertions.assertEquals(-2, service.validateForgotPasswordToken(uuid));
    }

    @Test
    void testValidateForgotPasswordTokenValid() {
        UserEntity user = createUserEntity();
        String uuid = UUID.randomUUID().toString();
        ForgotPasswordToken forgotPasswordToken = new ForgotPasswordToken(user, uuid);
        when(forgotPasswordTokenRepository.findByToken(uuid)).thenReturn(forgotPasswordToken);
        Assertions.assertEquals(0, service.validateForgotPasswordToken(uuid));
    }

    @Test
    void testGetUserByForgotPasswordTokenNotNull() {
        UserEntity user = createUserEntity();
        String uuid = UUID.randomUUID().toString();
        ForgotPasswordToken forgotPasswordToken = new ForgotPasswordToken(user, uuid);
        when(forgotPasswordTokenRepository.findByToken(uuid)).thenReturn(forgotPasswordToken);
        UserEntity userResult = service.getUserByForgotPasswordToken(uuid);
        Assertions.assertNotNull(userResult);
    }

    @Test
    void testGetUserByForgotPasswordTokenNull() {
        String uuid = UUID.randomUUID().toString();
        UserEntity userResult = service.getUserByForgotPasswordToken(uuid);
        Assertions.assertNull(userResult);
    }

    @Test
    void testDeleteForgotPasswordToken() {
        UserEntity user = createUserEntity();
        String uuid = UUID.randomUUID().toString();
        ForgotPasswordToken forgotPasswordToken = new ForgotPasswordToken(user, uuid);
        when(forgotPasswordTokenRepository.findByToken(uuid)).thenReturn(forgotPasswordToken);
        service.deleteForgotPasswordToken(uuid);
        verify(forgotPasswordTokenRepository, times(1)).delete(forgotPasswordToken);
    }

    private UserEntity createUserEntity() {
        UserEntity user = new UserEntity();
        user.setId(1L);
        user.setName("user");
        user.setMail("user@budgeteer.local");
        user.setMailVerified(true);
        user.setPassword(passwordHasher.hash("password"));
        user.setAuthorizedProjects(new ArrayList<ProjectEntity>());
        return user;
    }

    private Optional<UserEntity> createUserEntityOptional() {
        UserEntity user = new UserEntity();
        user.setId(1L);
        user.setName("user");
        user.setMail("user@budgeteer.local");
        user.setMailVerified(true);
        user.setPassword(passwordHasher.hash("password"));
        user.setAuthorizedProjects(new ArrayList<ProjectEntity>());
        return Optional.of(user);
    }

    private ProjectEntity createProjectEntity() {
        ProjectEntity project = new ProjectEntity();
        project.setId(1L);
        project.setName("name");
        project.setAuthorizedUsers(new ArrayList<UserEntity>());
        return project;
    }
}
