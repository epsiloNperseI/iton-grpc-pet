package org.example.grpc.service;

import com.example.numbers.NumberRange;
import com.example.numbers.NumberSequenceGrpc;
import com.example.numbers.NumberValue;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
public class NumberSequenceClientService {


    @GrpcClient("numberSequence")
    private NumberSequenceGrpc.NumberSequenceStub asyncStub;

    public void requestNumberSequence(int firstValue, int lastValue) {
        CountDownLatch finishLatch = new CountDownLatch(1);
        AtomicInteger lastNumberFromServer = new AtomicInteger();
        AtomicBoolean numberUpdated = new AtomicBoolean(false);

        NumberRange request = NumberRange.newBuilder()
                .setFirstValue(firstValue)
                .setLastValue(lastValue)
                .build();

        StreamObserver<NumberValue> responseObserver = new StreamObserver<>() {
            @Override
            public void onNext(NumberValue value) {
                System.out.println("new value: " + value.getValue());
                lastNumberFromServer.set(value.getValue());
                numberUpdated.set(true);
            }

            @Override
            public void onError(Throwable t) {
                finishLatch.countDown();
            }

            @Override
            public void onCompleted() {
                finishLatch.countDown();
            }
        };

        asyncStub.generateSequence(request, responseObserver);

        new Thread(() -> {
            int currentValue = 0;
            for (int i = 0; i <= 50; i++) {
                if (numberUpdated.getAndSet(false)) {
                    currentValue += lastNumberFromServer.get() + 1;
                } else {
                    currentValue += 1;
                }
                System.out.println("currentValue: " + currentValue);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }).start();

        try {
            finishLatch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

}
