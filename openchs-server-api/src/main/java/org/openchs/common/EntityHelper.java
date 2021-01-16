package org.openchs.common;

import org.openchs.dao.CHSRepository;
import org.openchs.domain.CHSEntity;
import org.openchs.web.request.CHSRequest;

import java.util.UUID;

public class EntityHelper<T extends CHSEntity> {

    public static <T extends CHSEntity> T newOrExistingEntity(CHSRepository<T> chsRepository, CHSRequest chsRequest, T chsEntity) {
        T t = chsRepository.findByUuid(chsRequest.getUuid());
        if (t == null) {
            t = chsEntity;
            t.setUuid(chsRequest.getUuid() != null ? chsRequest.getUuid() : UUID.randomUUID().toString());
        }
        return t;
    }
}
