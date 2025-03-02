package in.bushansirgur.foodiesapi.service;

import in.bushansirgur.foodiesapi.io.FoodRequest;
import in.bushansirgur.foodiesapi.io.FoodResponse;
import org.springframework.web.multipart.MultipartFile;

public interface FoodService {

    String uploadFile(MultipartFile file);

    FoodResponse addFood(FoodRequest request, MultipartFile file);
}
