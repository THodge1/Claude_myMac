import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

@JsonClassDescription("A tool that executes bash commands and returns the output.")
public class BashTool {
    @JsonPropertyDescription("The bash command to execute")
    @JsonProperty("command")
    public String command;
    
}
