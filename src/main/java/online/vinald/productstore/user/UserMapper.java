package online.vinald.productstore.user;

import org.springframework.stereotype.Component;

@Component
public class UserMapper {

	public UserResponseDto toResponseDto(User user) {
		return new UserResponseDto(
				user.getId(),
				user.getEmail(),
				user.getDisplayName(),
				user.getRole(),
				user.getCreatedAt(),
				user.getUpdatedAt()
		);
	}
}
