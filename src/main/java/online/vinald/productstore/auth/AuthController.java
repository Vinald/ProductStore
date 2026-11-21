package online.vinald.productstore.auth;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
	private final AuthService authService;
	private final RefreshTokenService refreshTokenService;

	@PostMapping("/register")
	@ResponseStatus(HttpStatus.CREATED)
	public AuthResponseDto register(@Valid @RequestBody AuthRequestDto authRequestDto) {
		return authService.register(authRequestDto);
	}

	@PostMapping("/login")
	@ResponseStatus(HttpStatus.OK)
	public AuthResponseDto login(@Valid @RequestBody AuthRequestDto authRequestDto) {
		return authService.login(authRequestDto);
	}

	@PostMapping("/refresh")
	@ResponseStatus(HttpStatus.OK)
	public AuthResponseDto refresh(@Valid @RequestBody RefreshRequestDto refreshRequestDto) {
		return authService.refresh(refreshRequestDto.refreshToken());
	}

	@PostMapping("/logout")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void logout(@Valid @RequestBody RefreshRequestDto refreshRequestDto) {
		refreshTokenService.deleteByToken(refreshRequestDto.refreshToken());
	}
}
