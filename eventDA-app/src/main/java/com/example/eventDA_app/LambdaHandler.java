package com.example.eventDA_app;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.S3Event;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;

public class LambdaHandler implements RequestHandler<S3Event, Void> {
    @Override
    public Void handleRequest(S3Event event, Context context) {
        String snsTopicArn = System.getenv("SNS_TOPIC_ARN");
        if (snsTopicArn == null) {
            context.getLogger().log("SNS_TOPIC_ARN environment variable not set");
            return null;
        }

        SnsClient snsClient = SnsClient.create();

        for (var record : event.getRecords()) {
            String bucket = record.getS3().getBucket().getName();
            String key = record.getS3().getObject().getKey();
            String message = "New object uploaded: s3://" + bucket + "/" + key;

            PublishRequest publishRequest = PublishRequest.builder()
                    .topicArn(snsTopicArn)
                    .message(message)
                    .build();

            try {
                snsClient.publish(publishRequest);
                context.getLogger().log("Published message to SNS: " + message);
            } catch (Exception e) {
                context.getLogger().log("Error publishing to SNS: " + e.getMessage());
            }
        }
        return null;
    }
}
