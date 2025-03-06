package in.bushansirgur.foodiesapi.repository;

import in.bushansirgur.foodiesapi.entity.CartEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CartRespository extends MongoRepository<CartEntity, String> {
}
