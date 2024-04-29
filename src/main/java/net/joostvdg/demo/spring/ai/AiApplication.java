package net.joostvdg.demo.spring.ai;

import org.springframework.ai.chat.ChatClient;
import org.springframework.ai.reader.ExtractedTextFormatter;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.reader.pdf.config.PdfDocumentReaderConfig;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Map;

@SpringBootApplication
public class AiApplication {

	public static void main(String[] args) {
		SpringApplication.run(AiApplication.class, args);
	}

	@Bean
	ApplicationRunner demo (
			VectorStore vectorStore,
			@Value("file://${HOME}/projects/spring-ai-01/medicaid-wa-faqs.pdf") Resource pdf,
			JdbcTemplate template,
			Chatbot myChatbot
	) {

		return args -> {
			var init_data = false;
			initialize_data(init_data, vectorStore, pdf, template);

			var response = myChatbot.chat("what should I know about the transition to consumer direct care network washington?");
			System.out.println(Map.of("response", response));
		};
	}

	private static void initialize_data(boolean doWork, VectorStore vectorStore, Resource pdf, JdbcTemplate template) {

		if (!doWork) {
			System.out.println("Skipping data initialization");
			return;
		}
		System.out.println("Initializing data");

		template.execute("delete from vector_store");

		var config = PdfDocumentReaderConfig
				.builder()
				.withPageExtractedTextFormatter(new ExtractedTextFormatter
						.Builder()
						.withNumberOfBottomTextLinesToDelete(3)
						.build())
				.build();

		var pdfReader =new PagePdfDocumentReader(pdf, config);

		var textSplitter = new TokenTextSplitter();

		var docs = textSplitter.apply(pdfReader.get());
		vectorStore.accept(docs);
	}
}
