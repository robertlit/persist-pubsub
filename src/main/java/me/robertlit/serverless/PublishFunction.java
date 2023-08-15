package me.robertlit.serverless;

import com.google.cloud.functions.HttpFunction;
import com.google.cloud.functions.HttpRequest;
import com.google.cloud.functions.HttpResponse;
import com.google.cloud.pubsub.v1.Publisher;
import com.google.protobuf.ByteString;
import com.google.pubsub.v1.ProjectTopicName;
import com.google.pubsub.v1.PubsubMessage;
import com.google.pubsub.v1.TopicName;

import java.io.IOException;
import java.util.stream.Collectors;

public class PublishFunction implements HttpFunction {

    private final Publisher publisher;

    public PublishFunction() throws IOException {
        String project = System.getenv("PROJECT");
        String pubsubTopic = System.getenv("PUBSUB_TOPIC");
        TopicName topicName = ProjectTopicName.of(project, pubsubTopic);
        this.publisher = Publisher.newBuilder(topicName).build();
    }

    @Override
    public void service(HttpRequest request, HttpResponse response) throws Exception {
        String data = request.getReader().lines().collect(Collectors.joining("\n"));

        PubsubMessage message = PubsubMessage.newBuilder()
                .setData(ByteString.copyFromUtf8(data))
                .build();
        publisher.publish(message);

        response.getWriter().write("OK");
    }
}
