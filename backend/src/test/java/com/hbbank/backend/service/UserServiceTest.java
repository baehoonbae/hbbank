package com.hbbank.backend.service;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.*;

import com.hbbank.backend.dto.LoginRequestDTO;
import com.hbbank.backend.dto.OAuth2AdditionalInfoDTO;
import com.hbbank.backend.exception.user.DuplicateUserException;
import com.hbbank.backend.exception.user.InvalidUserPasswordException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.mockito.ArgumentMatchers.any;

import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.hbbank.backend.domain.User;
import com.hbbank.backend.dto.UserRegistDTO;
import com.hbbank.backend.exception.user.UserNotFoundException;
import com.hbbank.backend.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder encoder;

    @InjectMocks
    private UserService userService;

    private User normalUser;
    private User oAuth2User1;   // 추가정보 입력이 필요한 oauth2 유저
    private User oAuth2User2;   // 추가정보 입력이 불필요한 oauth2 유저
    private UserRegistDTO dto1;
    private LoginRequestDTO dto2;
    private OAuth2AdditionalInfoDTO dto3;

    @BeforeEach
    @DisplayName("더미 유저, dto 생성")
    void init() {
        normalUser = User.builder()
                .id(1L)
                .name("일반유저")
                .birth(LocalDate.now())
                .username("test1")
                .password("1234")
                .address("서울")
                .phone("010-1234-1234")
                .email("test1@test")
                .emailVerified(true)
                .isOAuth2User(false)
                .needAdditionalInfo(false)
                .build();
        oAuth2User1 = User.builder()
                .id(1L)
                .name("oauth2 유저")
                .birth(LocalDate.now())
                .username("test2")
                .password("1234")
                .address("서울")
                .phone("010-1234-1234")
                .email("test2@test")
                .emailVerified(true)
                .isOAuth2User(true)
                .needAdditionalInfo(true)
                .build();
        oAuth2User2 = User.builder()
                .id(1L)
                .name("oauth2 유저")
                .birth(LocalDate.now())
                .username("test3")
                .password("1234")
                .address("서울")
                .phone("010-1234-1234")
                .email("test3@test")
                .emailVerified(true)
                .isOAuth2User(true)
                .needAdditionalInfo(false)
                .build();
        dto1 = UserRegistDTO.builder()
                .name(normalUser.getName())
                .birth(normalUser.getBirth())
                .username(normalUser.getUsername())
                .password(normalUser.getPassword())
                .address(normalUser.getAddress())
                .phone(normalUser.getPhone())
                .email(normalUser.getEmail())
                .build();
        dto2 = new LoginRequestDTO("test1", "1234");
        dto3 = OAuth2AdditionalInfoDTO.builder()
                .username("test1")
                .birth(LocalDate.of(1999, 6, 29))
                .address("서울이요")
                .phone("010-1234-1234")
                .build();
    }

    @Test
    @DisplayName("사용자 아이디로 조회 성공")
    void findById_Success() {
        //given
        Long userId = normalUser.getId();
        when(userRepository.findById(any(Long.class)))
                .thenReturn(Optional.of(normalUser));

        //when
        User user = userService.findById(userId);

        //then
        assertAll(
                () -> assertNotNull(user),
                () -> assertEquals(user.getId(), userId)
        );
        verify(userRepository).findById(userId);
    }

    @Test
    @DisplayName("사용자 아이디로 조회 실패 - 존재하지 않는 사용자")
    void findById_Fail_UserNotFound() {
        //given
        Long userId = normalUser.getId();
        when(userRepository.findById(any(Long.class)))
                .thenReturn(Optional.empty());

        //when & then
        assertThrows(UserNotFoundException.class, () -> userService.findById(userId));
        verify(userRepository).findById(any(Long.class));
    }

    @Test
    @DisplayName("사용자 등록 성공")
    void register_Success() {
        //given
        when(userRepository.save(any(User.class)))
                .thenAnswer(i -> i.getArgument(0));
        when(encoder.encode(anyString()))
                .thenReturn("ㅇㅇ");

        //when
        User user = userService.register(dto1);

        //then
        assertAll(
                () -> assertNotNull(user),
                () -> assertEquals(dto1.getName(), user.getName()),
                () -> assertEquals(dto1.getBirth(), user.getBirth()),
                () -> assertEquals(dto1.getUsername(), user.getUsername()),
                () -> assertEquals("ㅇㅇ", user.getPassword()),
                () -> assertEquals(dto1.getAddress(), user.getAddress()),
                () -> assertEquals(dto1.getPhone(), user.getPhone()),
                () -> assertEquals(dto1.getEmail(), user.getEmail())
        );
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("사용자 등록 실패 - 엔티티 unique 제약조건 위반")
    void register_Fail_DuplicateError() {
        //given
        when(userRepository.save(any(User.class)))
                .thenThrow(DataIntegrityViolationException.class);

        //when&then
        assertThrows(DuplicateUserException.class, () -> userService.register(dto1));
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("로그인 성공")
    void login_Success() {
        //given
        when(userRepository.findByUsername(anyString()))
                .thenReturn(Optional.of(normalUser));
        when(encoder.matches(anyString(), anyString()))
                .thenReturn(true);

        //when
        User user = userService.login(dto2);

        //then
        assertAll(
                () -> assertNotNull(user),
                () -> assertEquals(dto2.getUsername(), user.getUsername()),
                () -> assertEquals(dto2.getPassword(), user.getPassword())
        );
        verify(userRepository).findByUsername(anyString());
        verify(encoder).matches(anyString(), anyString());
    }

    @Test
    @DisplayName("로그인 실패 - 존재하지 않는 사용자")
    void login_Fail_UserNotFound() {
        //given
        when(userRepository.findByUsername(anyString()))
                .thenReturn(Optional.empty());

        //when & then
        assertThrows(UserNotFoundException.class, () -> userService.login(dto2));
        verify(userRepository).findByUsername(anyString());
    }

    @Test
    @DisplayName("로그인 실패 - 비밀번호 불일치")
    void login_Fail_InvalidPassword() {
        //given
        when(userRepository.findByUsername(anyString()))
                .thenReturn(Optional.of(normalUser));
        when(encoder.matches(anyString(), anyString()))
                .thenReturn(false);

        //when & then
        assertThrows(InvalidUserPasswordException.class, () -> userService.login(dto2));
        verify(encoder).matches(anyString(), anyString());
    }

    @Test
    @DisplayName("OAuth2 유저 등록 성공")
    void registerOAuth2_Success() {
        //given
        String email = oAuth2User1.getEmail();
        String name = oAuth2User1.getName();
        when(userRepository.save(any(User.class)))
                .thenAnswer(i -> i.getArgument(0));
        when(encoder.encode(anyString()))
                .thenReturn("ㅇㅇ");

        //when
        User user = userService.registOAuth2User(email, name);

        //then
        assertAll(
                () -> assertNotNull(user),
                () -> assertEquals(oAuth2User1.getEmail(), user.getEmail()),
                () -> assertEquals(oAuth2User1.getName(), user.getName()),
                () -> assertEquals(oAuth2User1.getUsername(), user.getUsername()),
                () -> assertEquals("ㅇㅇ", user.getPassword()),
                () -> assertEquals(oAuth2User1.isOAuth2User(), user.isOAuth2User()),
                () -> assertEquals(oAuth2User1.isNeedAdditionalInfo(), user.isNeedAdditionalInfo()),
                () -> assertEquals(LocalDate.of(1900, 1, 1), user.getBirth()),
                () -> assertEquals("서울", user.getAddress()),
                () -> assertEquals("010-0000-0000", user.getPhone()),
                () -> assertEquals(oAuth2User1.isEmailVerified(), user.isEmailVerified())
        );
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("사용자 이메일로 조회 성공")
    void findByEmail_Success() {
        //given
        when(userRepository.findByEmail(anyString()))
                .thenReturn(Optional.of(normalUser));

        //when
        User user = userService.findByEmail(normalUser.getEmail());

        //then
        assertAll(
                () -> assertNotNull(user),
                () -> assertEquals(normalUser.getEmail(), user.getEmail())
        );
        verify(userRepository).findByEmail(anyString());
    }

    @Test
    @DisplayName("사용자 이메일로 조회 실패 - 존재하지 않는 사용자")
    void findByEmail_Fail_UserNotFound() {
        //given
        when(userRepository.findByEmail(anyString()))
                .thenReturn(Optional.empty());

        //when &then
        assertThrows(UserNotFoundException.class, () -> userService.findByEmail("dd"));
        verify(userRepository).findByEmail(anyString());
    }

    @Test
    @DisplayName("OAuth2 추가 정보 등록 성공")
    void updateAdditionalInfo_Success() {
        //given
        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.of(oAuth2User1));
        when(userRepository.save(any(User.class)))
                .thenAnswer(i -> i.getArgument(0));

        //when
        User user = userService.updateAdditionalInfo(oAuth2User1.getId(), dto3);

        //then
        assertAll(
                () -> assertNotNull(user),
                () -> assertEquals("test1", user.getUsername()),
                () -> assertEquals(LocalDate.of(1999, 6, 29), user.getBirth()),
                () -> assertEquals("서울이요", user.getAddress()),
                () -> assertEquals("010-1234-1234", user.getPhone())
        );
        verify(userRepository).findById(anyLong());
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("OAuth2 추가 정보 등록 실패 - 존재하지 않는 사용자")
    void updateAdditionalInfo_Fail_UserNotFound() {
        //given
        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.empty());

        //when&then
        assertThrows(UserNotFoundException.class, () -> userService.updateAdditionalInfo(1L, dto3));
        verify(userRepository).findById(anyLong());
        verify(userRepository, times(0)).save(any(User.class));
    }

    @Test
    @DisplayName("OAuth2 추가 정보 등록 실패 - 엔티티 unique 제약조건 위반")
    void updateAdditionalInfo_Fail_DuplicateUsername() {
        //given
        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.of(oAuth2User1));
        when(userRepository.save(any(User.class)))
                .thenThrow(DataIntegrityViolationException.class);

        //when&then
        assertThrows(DuplicateUserException.class, () -> userService.updateAdditionalInfo(1L, dto3));
        verify(userRepository).findById(anyLong());
        verify(userRepository).save(any(User.class));
    }

}
