package eurodyn.poc;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import io.quarkiverse.langchain4j.RegisterAiService;
import io.smallrye.mutiny.Multi;
import jakarta.enterprise.context.ApplicationScoped;


@ApplicationScoped
@RegisterAiService(tools = DemoTools.class)
@SystemMessage("""
        	Your only goal is to get a confirmation from the user that they want to find the value in the Rest endpoint. 
                 Accept only explicit confirmations like "yes", "yes please", "go ahead", "proceed", or "that's correct".
                 Any response that isn't an unambiguous confirmation should trigger the question again:
                 Would you like to find the value in the Rest endpoint?
                 Only call the tool when you receive a clear confirmation
        """)
public interface ChatbotAgentService {
    Multi<String> chatMessage(@MemoryId String memoryId, @UserMessage String userMessage);

}