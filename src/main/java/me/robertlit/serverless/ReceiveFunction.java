package me.robertlit.serverless;

import com.google.cloud.WriteChannel;
import com.google.cloud.functions.CloudEventsFunction;
import com.google.cloud.storage.*;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.cloudevents.CloudEvent;
import io.cloudevents.CloudEventData;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.logging.Logger;

public class ReceiveFunction implements CloudEventsFunction {

    private static final String BUCKET_NAME = System.getenv("BUCKET_NAME");
    private static final String LOGS_FOLDER_NAME = "logs";
    private static final String CONTENT_TYPE_TEXT = "text/plain";

    private final Logger logger = Logger.getLogger("ReceiverFunction");
    private final Storage storage = StorageOptions.getDefaultInstance().getService();
    private final Gson gson = new Gson();

    @Override
    public void accept(CloudEvent event) {
        logger.info("Received pubsub message");
        CloudEventData data = event.getData();

        if (data == null) {
            logger.info("No event data for " + event.getId());
            return;
        }

        Message message = parseMessage(data.toBytes());
        Blob blob = storage.create(getBlobInfo(getFileName(message)));

        try (WriteChannel writer = blob.writer()) {
            writer.write(ByteBuffer.wrap(gson.toJson(message).getBytes()));
        } catch (IOException exception) {
            logger.severe(exception.toString());
        }

        logger.info("Processed message with id " + message.messageId());
    }

    private Message parseMessage(byte[] bytes) {
        String jsonData = new String(bytes);
        JsonObject jsonMessage = JsonParser.parseString(jsonData)
                .getAsJsonObject()
                .getAsJsonObject("message");
        return gson.fromJson(jsonMessage, Message.class);
    }

    private String getFileName(Message message) {
        return LOGS_FOLDER_NAME + "/" + message.messageId();
    }

    private BlobInfo getBlobInfo(String fileName) {
        BlobId blobId = BlobId.of(BUCKET_NAME, fileName);
        return BlobInfo.newBuilder(blobId)
                .setContentType(CONTENT_TYPE_TEXT)
                .build();
    }

    private record Message(String data, String messageId, String publishTime) {

    }
}
