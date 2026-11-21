package online.vinald.productstore.user;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/users")
@RequiredArgsConstructor
public class UserController {
	private final UserService userService;

	@GetMapping("/me")
	@ResponseStatus(HttpStatus.OK)
	public UserResponseDto getProfile(@AuthenticationPrincipal User currentUser) {
		return userService.getProfile(currentUser);
	}

	@PutMapping("/me")
	@ResponseStatus(HttpStatus.OK)
	public UserResponseDto updateProfile(@Valid @RequestBody UserProfileUpdateRequestDto userProfileUpdateRequestDto,
										  @AuthenticationPrincipal User currentUser) {
		return userService.updateProfile(currentUser, userProfileUpdateRequestDto);
	}
}
