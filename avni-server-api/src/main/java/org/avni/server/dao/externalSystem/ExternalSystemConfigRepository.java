package org.avni.server.dao.externalSystem;

import org.avni.messaging.domain.GlificSystemConfig;
import org.avni.server.dao.AvniCrudRepository;
import org.avni.server.domain.extenalSystem.ExternalSystemConfig;
import org.avni.server.domain.extenalSystem.SystemName;
import org.springframework.stereotype.Repository;

@Repository
public interface ExternalSystemConfigRepository extends AvniCrudRepository<ExternalSystemConfig, Long> {

    ExternalSystemConfig findBySystemName(SystemName systemName);

    ExternalSystemConfig findBySystemNameAndOrganisationId(SystemName systemName, Long organisationId);

    default GlificSystemConfig getGlificSystemConfig(Long organisationId) {
        return new GlificSystemConfig(findBySystemNameAndOrganisationId(SystemName.Glific, organisationId));
    }
}
