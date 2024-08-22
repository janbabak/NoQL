package com.janbabak.noqlcustommodel.publicApi;

import com.janbabak.noqlcustommodel.gptApi.GptApiService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/", produces = MediaType.APPLICATION_JSON_VALUE)
@CrossOrigin
@RequiredArgsConstructor
public class PublicApi {

    private final GptApiService gptApiService;

    // TODO: response query response
    @PostMapping("/query")
    public Object query(@RequestBody ModelRequest request) {
        return gptApiService.queryModel(request);
    }
}
