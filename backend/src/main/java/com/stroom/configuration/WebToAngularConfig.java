package com.stroom.configuration;

import org.springframework.boot.autoconfigure.web.ErrorViewResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.Map;

/**
 * Created by Stroom on 11/06/2017.
 */
@Configuration
public class WebToAngularConfig {
	//https://stackoverflow.com/questions/38516667/springboot-angular2-how-to-handle-html5-urls
	@Bean
	ErrorViewResolver supportPathBasedLocationStrategyWithoutHashes() {
		return new ErrorViewResolver() {
			@Override
			public ModelAndView resolveErrorView(HttpServletRequest request, HttpStatus status, Map<String, Object> model) {
				return status == HttpStatus.NOT_FOUND || status == HttpStatus.UNAUTHORIZED
						? new ModelAndView("index.html", Collections.<String, Object>emptyMap(), HttpStatus.OK)
						: null;
			}
		};
	}
}
