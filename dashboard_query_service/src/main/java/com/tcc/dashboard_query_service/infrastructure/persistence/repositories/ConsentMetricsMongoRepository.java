package com.tcc.dashboard_query_service.infrastructure.persistence.repositories;

import com.tcc.dashboard_query_service.infrastructure.persistence.documents.ConsentMetricsDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ConsentMetricsMongoRepository extends MongoRepository<ConsentMetricsDocument, String> {
    Optional<ConsentMetricsDocument> findById(String id);
}