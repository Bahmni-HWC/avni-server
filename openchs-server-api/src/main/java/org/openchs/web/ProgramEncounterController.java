package org.openchs.web;

import org.openchs.dao.*;
import org.openchs.domain.*;
import org.openchs.service.ObservationService;
import org.openchs.web.request.ProgramEncounterRequest;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.transaction.Transactional;

@RestController
public class ProgramEncounterController extends AbstractController<ProgramEncounter> {
    private EncounterTypeRepository encounterTypeRepository;
    private ProgramEncounterRepository programEncounterRepository;
    private ProgramEnrolmentRepository programEnrolmentRepository;
    private ObservationService observationService;

    private static org.slf4j.Logger logger = LoggerFactory.getLogger(IndividualController.class);

    @Autowired
    public ProgramEncounterController(EncounterTypeRepository encounterTypeRepository, ProgramEncounterRepository programEncounterRepository, ProgramEnrolmentRepository programEnrolmentRepository, ObservationService observationService) {
        this.encounterTypeRepository = encounterTypeRepository;
        this.programEncounterRepository = programEncounterRepository;
        this.programEnrolmentRepository = programEnrolmentRepository;
        this.observationService = observationService;
    }

    @RequestMapping(value = "/programEncounters", method = RequestMethod.POST)
    @Transactional
    @PreAuthorize(value = "hasAnyAuthority('user', 'admin')")
    public void save(@RequestBody ProgramEncounterRequest request) {
        logger.info(String.format("Saving programEncounter with uuid %s", request.getUuid()));
        EncounterType encounterType = (EncounterType) ReferenceDataRepositoryImpl.findReferenceEntity(encounterTypeRepository, request.getEncounterType(), request.getEncounterTypeUUID());

        ProgramEncounter encounter = newOrExistingEntity(programEncounterRepository, request, new ProgramEncounter());
        encounter.setEncounterDateTime(request.getEncounterDateTime());
        encounter.setProgramEnrolment(programEnrolmentRepository.findByUuid(request.getProgramEnrolmentUUID()));
        encounter.setEncounterType(encounterType);
        encounter.setObservations(observationService.createObservations(request.getObservations()));
        encounter.setName(request.getName());
        encounter.setEarliestVisitDateTime(request.getEarliestVisitDateTime());
        encounter.setMaxVisitDateTime(request.getMaxVisitDateTime());
        encounter.setCancelDateTime(request.getCancelDateTime());
        encounter.setCancelObservations(observationService.createObservations(request.getCancelObservations()));

        programEncounterRepository.save(encounter);
        logger.info(String.format("Saved programEncounter with uuid %s", request.getUuid()));
    }
}