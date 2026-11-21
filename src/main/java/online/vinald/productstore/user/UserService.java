package online.vinald.productstore.user;

import lombok.RequiredArgsConstructor;
import online.vinald.productstore.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {
	private final UserRepository userRepository;
	private final UserMapper userMapper;

	@Transactional(readOnly = true)
	public UserResponseDto getProfile(User currentUser) {
		return userMapper.toResponseDto(currentUser);
	}

	public UserResponseDto updateProfile(User currentUser, UserProfileUpdateRequestDto userProfileUpdateRequestDto) {
		User user = userRepository.findById(currentUser.getId())
				.orElseThrow(() -> new ResourceNotFoundException("User not found"));

		user.setDisplayName(userProfileUpdateRequestDto.displayName());

		return userMapper.toResponseDto(userRepository.save(user));
	}
}
