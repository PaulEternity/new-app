package com.paul.appgen.controller;

import com.paul.appgen.common.BaseResponse;
import com.paul.appgen.common.ResultUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/health")
public class HealthController {
/**
 * API endpoint for performing a check operation
 * This method handles GET requests to the /check endpoint
 *
 * @return BaseResponse containing a success message
 *         The response will have a status of success and contain the string "ok"
 */
    @GetMapping("/check")    // Maps this method to handle HTTP requests at the /check path
    public BaseResponse<String> check() {    // Method definition that returns a BaseResponse with String data
        return ResultUtils.success("ok");    // Returns a success response with "ok" as the data
    }
}
