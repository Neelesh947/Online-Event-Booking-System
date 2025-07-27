package in.neelesh.online.shopping.service.topic;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

import in.neelesh.online.shopping.utils.Constants;

@Configuration
public class KafkaLoginTopic {

	@Bean
	public NewTopic loginTopic() {
		return TopicBuilder.name(Constants.LOGIN_TOPIC).build();
	}
}