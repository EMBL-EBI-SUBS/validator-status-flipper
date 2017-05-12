package uk.ac.ebi.subs.validator.flipper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitMessagingTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.ac.ebi.subs.data.component.Archive;
import uk.ac.ebi.subs.validator.data.ValidationOutcome;
import uk.ac.ebi.subs.validator.data.ValidationOutcomeEnum;
import uk.ac.ebi.subs.validator.messaging.Queues;
import uk.ac.ebi.subs.validator.repository.ValidationOutcomeRepository;

import java.util.Map;

@Service
public class UpdatesListener {
    public static final Logger logger = LoggerFactory.getLogger(UpdatesListener.class);

    private RabbitMessagingTemplate rabbitMessagingTemplate;

    @Autowired
    private ValidationOutcomeRepository repository;

    @Autowired
    public UpdatesListener(RabbitMessagingTemplate rabbitMessagingTemplate) {
        this.rabbitMessagingTemplate = rabbitMessagingTemplate;
    }

    @RabbitListener(queues = Queues.OUTCOME_DOCUMENT_UPDATE)
    public void processUpdate(String uuid) {
        logger.debug("Processing Outcome document update with id {}.", uuid);
        ValidationOutcome validationOutcome = repository.findOne(uuid);

        flipStatusIfRequired(validationOutcome, uuid);
    }

    private void flipStatusIfRequired(ValidationOutcome validationOutcome, String uuid) {
        Map<Archive, Boolean> validationResults = validationOutcome.getExpectedOutcomes();
        if (!validationResults.values().contains(false)){
            validationOutcome.setValidationOutcome(ValidationOutcomeEnum.Complete);
            repository.save(validationOutcome);

            logger.info("Validation outcome document with id {} is completed.", uuid);
        }
    }
}