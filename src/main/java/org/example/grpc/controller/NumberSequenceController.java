package org.example.grpc.controller;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.example.grpc.service.NumberSequenceClientService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class NumberSequenceController {

    NumberSequenceClientService clientService;

    @GetMapping("/startSequence")
    public ResponseEntity<String> startSequence() {
        // Здесь мы асинхронно вызываем gRPC сервис,
        // так как не хотим блокировать HTTP запрос ожиданием ответа от gRPC сервиса.
        new Thread(() -> {
            clientService.requestNumberSequence(0, 30);
        }).start();

        // HTTP ответ можно отправить сразу, не дожидаясь выполнения gRPC вызова.
        return ResponseEntity.ok("Sequence started");
    }

}
