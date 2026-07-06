package com.calendario.hrnest.api.user;

import com.calendario.hrnest.application.user.GetMyProfileUseCase;
import com.calendario.hrnest.application.user.ListAllUsersUseCase;
import com.calendario.hrnest.application.user.UpdateMyPersonalInfoUseCase;
import com.calendario.hrnest.application.user.UpdatePersonalInfoCommand;
import com.calendario.hrnest.application.user.UpdateUserOrganizationCommand;
import com.calendario.hrnest.application.user.UpdateUserOrganizationUseCase;
import com.calendario.hrnest.application.user.UpdateUserRoleCommand;
import com.calendario.hrnest.application.user.UpdateUserRoleUseCase;
import com.calendario.hrnest.application.user.UserProfileView;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final GetMyProfileUseCase getMyProfileUseCase;
    private final UpdateUserOrganizationUseCase updateUserOrganizationUseCase;
    private final UpdateMyPersonalInfoUseCase updateMyPersonalInfoUseCase;
    private final ListAllUsersUseCase listAllUsersUseCase;
    private final UpdateUserRoleUseCase updateUserRoleUseCase;

    public UserController(GetMyProfileUseCase getMyProfileUseCase,
                           UpdateUserOrganizationUseCase updateUserOrganizationUseCase,
                           UpdateMyPersonalInfoUseCase updateMyPersonalInfoUseCase,
                           ListAllUsersUseCase listAllUsersUseCase,
                           UpdateUserRoleUseCase updateUserRoleUseCase) {
        this.getMyProfileUseCase = getMyProfileUseCase;
        this.updateUserOrganizationUseCase = updateUserOrganizationUseCase;
        this.updateMyPersonalInfoUseCase = updateMyPersonalInfoUseCase;
        this.listAllUsersUseCase = listAllUsersUseCase;
        this.updateUserRoleUseCase = updateUserRoleUseCase;
    }

    @GetMapping("/me/profile")
    public ResponseEntity<UserProfileView> myProfile() {
        return ResponseEntity.ok(getMyProfileUseCase.execute());
    }

    @GetMapping
    public ResponseEntity<List<UserProfileView>> listAll() {
        return ResponseEntity.ok(listAllUsersUseCase.execute());
    }

    @PatchMapping("/{id}/profile")
    public ResponseEntity<UserProfileView> updateProfile(@PathVariable Long id,
                                                          @Valid @RequestBody UpdateUserProfileRequest request) {
        UserProfileView updated = updateUserOrganizationUseCase.execute(new UpdateUserOrganizationCommand(
                id, request.position(), request.department(), request.facility(), request.supervisorId()));
        return ResponseEntity.ok(updated);
    }

    @PatchMapping("/{id}/role")
    public ResponseEntity<UserProfileView> updateRole(@PathVariable Long id,
                                                       @Valid @RequestBody UpdateUserRoleRequest request) {
        UserProfileView updated = updateUserRoleUseCase.execute(new UpdateUserRoleCommand(id, request.role()));
        return ResponseEntity.ok(updated);
    }

    @PatchMapping("/me/personal-info")
    public ResponseEntity<UserProfileView> updateMyPersonalInfo(
            @Valid @RequestBody UpdatePersonalInfoRequest request) {
        UserProfileView updated = updateMyPersonalInfoUseCase.execute(
                new UpdatePersonalInfoCommand(request.birthDate(), request.phoneNumber(), request.avatarUrl()));
        return ResponseEntity.ok(updated);
    }
}
