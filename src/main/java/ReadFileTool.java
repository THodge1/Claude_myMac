import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

@JsonClassDescription("A tool that reads the contents of a file and returns it as a string.")
public class ReadFileTool {
    @JsonProperty("filePath")
    @JsonPropertyDescription("The path to the file to read.")
    public String filePath;

}

