import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.core.JsonValue;
import com.openai.models.FunctionDefinition;
import com.openai.models.FunctionParameters;
import com.openai.models.chat.completions.ChatCompletion;
import com.openai.models.chat.completions.ChatCompletionAssistantMessageParam;
import com.openai.models.chat.completions.ChatCompletionCreateParams;
import com.openai.models.chat.completions.ChatCompletionMessage;
import com.openai.models.chat.completions.ChatCompletionMessageParam;
import com.openai.models.chat.completions.ChatCompletionTool;
import com.openai.models.chat.completions.ChatCompletionToolMessageParam;
import com.openai.models.chat.completions.ChatCompletionUserMessageParam;



public class Main {
    public static void main(String[] args) {
        if (args.length < 2 || !"-p".equals(args[0])) {
            System.err.println("Usage: program -p <prompt>");
            System.exit(1);
        }

        String prompt = args[1];

        String apiKey = System.getenv("OPENROUTER_API_KEY");
        String baseUrl = System.getenv("OPENROUTER_BASE_URL");
        if (baseUrl == null || baseUrl.isEmpty()) {
            baseUrl = "https://openrouter.ai/api/v1";
        }

        if (apiKey == null || apiKey.isEmpty()) {
            throw new RuntimeException("OPENROUTER_API_KEY is not set");
        }

        OpenAIClient client = OpenAIOkHttpClient.builder()
                .apiKey(apiKey)
                .baseUrl(baseUrl)
                .build();

        ChatCompletionTool readBuild = 
            tool(
                    "Read",
                    "Read and return the contents of a file",
                    Map.of(
                            "file_path",
                            Map.of(
                                    "type",
                                    "string",
                                    "description",
                                    "The path to the file to read")),
                    List.of("file_path"));



        
        
        List<ChatCompletionMessageParam> messages = new ArrayList<>();

        ChatCompletionUserMessageParam innerUser = ChatCompletionUserMessageParam.builder()
        .content(prompt)
        .build();

        ChatCompletionMessageParam userMessage = ChatCompletionMessageParam.ofUser(innerUser);

        messages.add(userMessage);

        
        while (true) {
        
        
        
        ChatCompletion response =
                client.chat()
                        .completions()
                        .create(
                                ChatCompletionCreateParams.builder()
                                        .model("anthropic/claude-haiku-4.5")
                                        .tools(List.of(readBuild))
                                        .messages(messages)
                                        .build());

        ChatCompletionMessage message = response.choices().get(0).message();
        var tCalls = message.toolCalls();

        ChatCompletionAssistantMessageParam assistantParam = message.toParam();
        ChatCompletionMessageParam assistantMessage = ChatCompletionMessageParam.ofAssistant(assistantParam);
        messages.add(assistantMessage);

        if (tCalls.isPresent()) {
            //throw new RuntimeException("tool calls are not supported yet");

            // get tool call, etc.
            var toolCall = tCalls.get().get(0);
            var function = toolCall.function();
            String toolName = function.name();
            String argumentsJson = function.arguments();
            
            String toolCallId = toolCall.id();
            String result = executeReadTool(argumentsJson);

            ChatCompletionToolMessageParam toolParam = ChatCompletionToolMessageParam.builder()
            .toolCallId(toolCallId)
            .content(result)
            .build();


            ChatCompletionMessageParam toolMessage = ChatCompletionMessageParam.ofTool(toolParam);
            messages.add(toolMessage);
            

        } else {
            System.out.print(response.choices().get(0).message().content().orElse(""));
            break;
        }

        // You can use print statements as follows for debugging, they'll be visible when running tests.
        System.err.println("Logs from your program will appear here!");

    }
}


    private static String executeReadTool(String argumentsJson) {
        ObjectMapper mapper = new ObjectMapper();
        ReadFileTool parsed;
        try {
                // try catch for parsing the argumentsJson into ReadFileTool
                parsed = mapper.readValue(argumentsJson, ReadFileTool.class);
            } catch (Exception e) {
                throw new RuntimeException("Failed to parse tool arguments", e);
            }

            parsed.filePath = parsed.filePath.trim();
            
            Path filePath = Path.of(parsed.filePath);

            String fileContents;
            try {
                fileContents = Files.readString(filePath);
            } catch (Exception e) {
                throw new RuntimeException("Failed to read file: " + parsed.filePath, e);
            }
            return fileContents;
    }

    private static ChatCompletionTool tool(
            String name,
            String description,
            Map<String, Object> properties,
            List<String> required) {
        return ChatCompletionTool.builder()
                .function(
                        FunctionDefinition.builder()
                                .name(name)
                                .description(description)
                                .parameters(
                                        FunctionParameters.builder()
                                                .putAdditionalProperty(
                                                        "type", JsonValue.from("object"))
                                                .putAdditionalProperty(
                                                        "properties", JsonValue.from(properties))
                                                .putAdditionalProperty(
                                                        "required", JsonValue.from(required))
                                                .build())
                                .build())
                .build();
    }
}
