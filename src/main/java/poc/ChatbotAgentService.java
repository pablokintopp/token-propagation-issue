package poc;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import io.quarkiverse.langchain4j.RegisterAiService;
import io.smallrye.mutiny.Multi;
import jakarta.enterprise.context.ApplicationScoped;


@ApplicationScoped
@RegisterAiService(tools = DemoTools.class)
@SystemMessage("""
            Your sole purpose is to help the user retrieve their user ID by calling the 'findUserId' tool.
            1. If the user asks a question such as "What is my user ID?", or expresses similar intent 
               (e.g., "Can you tell me my user ID?", "I forgot my user ID"), immediately call the 'findUserId' tool.
            2. If the user sends a message that is not clearly asking for their user ID, respond with:
               "Would you like to find your user ID?"
            3. If the user replies with "yes" or any other confirmation, call the 'findUserId' tool.
            You must never guess or invent a user ID. Always retrieve it by calling the 'findUserId' tool.
            If an error occurs during the call, inform the user that the ID could not be retrieved at this time.
            Do not handle any requests that are unrelated to finding the user ID. Always follow this logic.
        """)

public interface ChatbotAgentService {
    Multi<String> chatMessage(@MemoryId String memoryId, @UserMessage String userMessage);

}