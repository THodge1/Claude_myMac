import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.openai.models.chat.completions.ChatCompletionTool;

@JsonClassDescription("Read and return the contents of a file")
public class ReadFileTool {

    static ChatCompletionTool asChatCompletionTool() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @JsonPropertyDescription("The path to the file to read")
    @JsonProperty("file_path")
    public String filePath;
}

