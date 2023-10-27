package com.techorgx.api.repository

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression
import com.techorgx.api.model.Ad
import com.techorgx.api.util.AdStatus
import io.grpc.Status
import io.grpc.StatusException
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.springframework.stereotype.Component

@Component
class AdsRepositoryImpl(
    private val dynamoDBMapper: DynamoDBMapper,
) : AdsRepository {
    override fun <S : Ad?> save(entity: S): S {
        try {
            dynamoDBMapper.save(entity)
        } catch (e: Exception) {
            logger.error(e)
            throw StatusException(Status.INTERNAL.withDescription("Ad can not be saved"))
        }
        return entity
    }

    override fun findById(id: String): Ad? {
        try {
            return dynamoDBMapper.load(Ad::class.java, id)
        } catch (e: Exception) {
            logger.error(e)
        }
        return null
    }

    override fun updateAdStatus(
        id: String,
        status: AdStatus,
    ) {
        val ad = findById(id)
        ad?.let {
            it.status = status.name
            save(ad)
        } ?: throw StatusException(Status.INTERNAL.withDescription("Ad can not be updated"))
    }

    override fun getAdsByUser(id: String): List<Ad> {
        val query = DynamoDBQueryExpression<Ad>()
        val ad = Ad(username = id)
        query.hashKeyValues = ad
        return dynamoDBMapper.query(Ad::class.java, query)
    }

    private companion object {
        val logger: Logger = LogManager.getLogger(AdsRepositoryImpl::class.java)
    }
}
