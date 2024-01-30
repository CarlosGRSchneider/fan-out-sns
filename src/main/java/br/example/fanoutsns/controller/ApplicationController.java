package br.example.fanoutsns.controller;

import br.example.fanoutsns.entity.Order;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;
import software.amazon.awssdk.services.sns.model.PublishResponse;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageResponse;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/orders")
public class ApplicationController {

    @PostMapping
    public String processOrder(@RequestBody Order order) {

        String accessKeyId = System.getenv("AWS_ACCESS_KEY_ID");
        String secretAccessKey = System.getenv("AWS_SECRET_ACCESS_KEY");
        String snsTopicArn = System.getenv("SNS_TOPIC_ARN");

        AwsBasicCredentials awsCredentials = AwsBasicCredentials.create(accessKeyId, secretAccessKey);

        SnsClient snsClient = SnsClient.builder()
                .credentialsProvider(() -> awsCredentials)
                .region(Region.SA_EAST_1)
                .build();

        PublishRequest publishRequest = PublishRequest.builder()
                .topicArn(snsTopicArn)
                .message(order.toString())
                .messageGroupId("order")
                .messageDeduplicationId(UUID.randomUUID().toString())
                .build();

        PublishResponse publishResponse = snsClient.publish(publishRequest);

        snsClient.close();

        return publishResponse.messageId();
    }

//    @PostMapping
//    public List<String> processOrder(@RequestBody Order order) {
//        String sqsUrlFisco = "https://sqs.sa-east-1.amazonaws.com/674498201133/emissao-fiscal-compra";
//        String sqsUrlPosVendas = "https://sqs.sa-east-1.amazonaws.com/674498201133/fila-pos-venda";
//        String accessKeyId = System.getenv("AWS_ACCESS_KEY_ID");
//        String secretAccessKey = System.getenv("AWS_SECRET_ACCESS_KEY");
//
//        AwsBasicCredentials awsCredentials = AwsBasicCredentials.create(accessKeyId, secretAccessKey);
//
//        SqsClient sqsClient = SqsClient.builder()
//                .region(Region.SA_EAST_1)
//                .credentialsProvider(StaticCredentialsProvider.create(awsCredentials))
//                .build();
//
//        SendMessageRequest sendMessageRequestFisco = SendMessageRequest.builder()
//                .queueUrl(sqsUrlFisco)
//                .messageBody(order.toString())
//                .build();
//
//        SendMessageResponse sendMessageResponseFisco = sqsClient.sendMessage(sendMessageRequestFisco);
//
//        SendMessageRequest sendMessageRequestPosVenda = SendMessageRequest.builder()
//                .queueUrl(sqsUrlPosVendas)
//                .messageBody(order.toString())
//                .build();
//
//        SendMessageResponse sendMessageResponsePosVendas = sqsClient.sendMessage(sendMessageRequestPosVenda);
//
//        sqsClient.close();
//        return List.of(sendMessageResponseFisco.messageId(), sendMessageResponsePosVendas.messageId());
//    }
}
