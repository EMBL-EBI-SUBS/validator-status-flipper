package uk.ac.ebi.subs.validator.flipper;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.ac.ebi.subs.data.component.Archive;
import uk.ac.ebi.subs.validator.data.ValidationOutcome;
import uk.ac.ebi.subs.validator.data.ValidationOutcomeEnum;
import uk.ac.ebi.subs.validator.repository.ValidationOutcomeRepository;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 * Created by karoly on 18/05/2017.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@EnableMongoRepositories(basePackageClasses = ValidationOutcomeRepository.class)
@EnableAutoConfiguration
@SpringBootTest(classes = OutcomeDocumentService.class)
public class OutcomeDocumentServiceTest {

    @Autowired
    ValidationOutcomeRepository repository;

    @Autowired
    OutcomeDocumentService service;

    private ValidationOutcome existingValidationOutcome;

    @Before
    public void setUp() {
        repository.deleteAll();
        existingValidationOutcome = createValidationOutcome(getInitialExpectedOutcomes(), 3, "123", "456");
        repository.insert(existingValidationOutcome);
    }

    @Test
    public void updatingNotExistingOutcomeDocumentReturnFalse() {
        repository.delete(existingValidationOutcome);

        assertThat(service.updateValidationOutcome(existingValidationOutcome.getUuid()), is(false));
    }

    @Test
    public void notAllEntityHasBeenValidatedShouldLeaveOutcomePending() {
        assertThat(existingValidationOutcome.getValidationOutcome() == ValidationOutcomeEnum.Pending, is(true));

        existingValidationOutcome.getExpectedOutcomes().put(Archive.ArrayExpress, true);
        repository.save(existingValidationOutcome);

        service.updateValidationOutcome(existingValidationOutcome.getUuid());

        ValidationOutcome actualValidationOutcomeDocument = repository.findOne(existingValidationOutcome.getUuid());

        assertThat(actualValidationOutcomeDocument.getValidationOutcome() == ValidationOutcomeEnum.Pending, is(true));
        assertThat(actualValidationOutcomeDocument.getExpectedOutcomes().get(Archive.ArrayExpress), is(true));
        assertThat(actualValidationOutcomeDocument.getExpectedOutcomes().get(Archive.BioSamples), is(false));
        assertThat(actualValidationOutcomeDocument.getExpectedOutcomes().get(Archive.Ena), is(false));

    }

    @Test
    public void allEntityHasBeenValidatedShouldChangeOutcomeToComplete() {
        assertThat(existingValidationOutcome.getValidationOutcome() == ValidationOutcomeEnum.Pending, is(true));

        existingValidationOutcome.getExpectedOutcomes().put(Archive.ArrayExpress, true);
        existingValidationOutcome.getExpectedOutcomes().put(Archive.BioSamples, true);
        existingValidationOutcome.getExpectedOutcomes().put(Archive.Ena, true);
        repository.save(existingValidationOutcome);

        service.updateValidationOutcome(existingValidationOutcome.getUuid());

        ValidationOutcome actualValidationOutcomeDocument = repository.findOne(existingValidationOutcome.getUuid());

        assertThat(actualValidationOutcomeDocument.getValidationOutcome() == ValidationOutcomeEnum.Complete, is(true));
        assertThat(actualValidationOutcomeDocument.getExpectedOutcomes().get(Archive.ArrayExpress), is(true));
        assertThat(actualValidationOutcomeDocument.getExpectedOutcomes().get(Archive.BioSamples), is(true));
        assertThat(actualValidationOutcomeDocument.getExpectedOutcomes().get(Archive.Ena), is(true));

    }

    private ValidationOutcome createValidationOutcome(Map<Archive, Boolean> expectedOutcomes, int version, String submissionId, String entityUuid) {
        ValidationOutcome validationOutcome = new ValidationOutcome();
        validationOutcome.setUuid(UUID.randomUUID().toString());
        validationOutcome.setExpectedOutcomes(expectedOutcomes);
        validationOutcome.setVersion(version);
        validationOutcome.setSubmissionId(submissionId);
        validationOutcome.setEntityUuid(entityUuid);

        return validationOutcome;
    }

    private Map<Archive, Boolean> getInitialExpectedOutcomes() {
        Map<Archive, Boolean> expectedOutcomes = new HashMap<>();
        expectedOutcomes.put(Archive.ArrayExpress, false);
        expectedOutcomes.put(Archive.BioSamples, false);
        expectedOutcomes.put(Archive.Ena, false);
        return expectedOutcomes;
    }
}
