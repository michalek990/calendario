package com.calendario.hrnest.api.notification;

import com.calendario.hrnest.application.notification.ListMyNotificationsUseCase;
import com.calendario.hrnest.application.notification.MarkNotificationAsReadUseCase;
import com.calendario.hrnest.application.notification.NotificationView;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final ListMyNotificationsUseCase listMyNotificationsUseCase;
    private final MarkNotificationAsReadUseCase markNotificationAsReadUseCase;

    public NotificationController(ListMyNotificationsUseCase listMyNotificationsUseCase,
                                   MarkNotificationAsReadUseCase markNotificationAsReadUseCase) {
        this.listMyNotificationsUseCase = listMyNotificationsUseCase;
        this.markNotificationAsReadUseCase = markNotificationAsReadUseCase;
    }

    @GetMapping("/me")
    public ResponseEntity<List<NotificationView>> listMine() {
        return ResponseEntity.ok(listMyNotificationsUseCase.execute());
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<NotificationView> markAsRead(@PathVariable Long id) {
        return ResponseEntity.ok(markNotificationAsReadUseCase.execute(id));
    }
}
