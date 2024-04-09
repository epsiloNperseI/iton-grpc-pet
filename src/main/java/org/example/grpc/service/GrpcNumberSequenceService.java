package org.example.grpc.service;

import com.example.numbers.NumberRange;
import com.example.numbers.NumberSequenceGrpc;
import com.example.numbers.NumberValue;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;

@GrpcService
public class GrpcNumberSequenceService  extends NumberSequenceGrpc.NumberSequenceImplBase {
    @Override
    public void generateSequence(NumberRange request, StreamObserver<NumberValue> responseObserver) {
        int firstValue = request.getFirstValue();
        int lastValue = request.getLastValue();

        new Thread(() -> {
            for (int i = firstValue; i <= lastValue; i++) {
                NumberValue number = NumberValue.newBuilder().setValue(i).build();
                responseObserver.onNext(number);
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    responseObserver.onError(e);
                    Thread.currentThread().interrupt();
                }
            }
            responseObserver.onCompleted();
        }).start();
    }

}
