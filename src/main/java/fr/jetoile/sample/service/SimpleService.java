/*
 * Copyright (c) 2011 Khanh Tuong Maudoux <kmx.petals@gmail.com>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package fr.jetoile.sample.service;

import com.codahale.metrics.Timer;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;
import fr.jetoile.sample.Main;
import fr.jetoile.sample.dto.DtoResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.time.LocalDateTime;

import static com.codahale.metrics.MetricRegistry.name;

@Api(value = "/sample",
        description = "the sample api")
@Path("/sample")
@RolesAllowed("admin")
public class SimpleService {
    private final static Logger log = LoggerFactory.getLogger(SimpleService.class);


    @GET
    @Path("/say/{msg}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "repeat the word",
            notes = "response the word",
            response = DtoResponse.class)
    @ApiResponses(value = {@ApiResponse(code = 500, message = "Internal server error")})
    public Response sayHello(@PathParam("msg") String message) {

        final Timer timer = Main.metricRegistry.timer(name(SimpleService.class, "say-service"));
        final Timer.Context context = timer.time();
        try {

            DtoResponse response = new DtoResponse();
            try {
                response.setMessage(message);
                response.setTime(LocalDateTime.now());
            } catch (Exception e) {
                log.error("internal error: {}", e);
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
            }
            return Response.ok(response).build();
        } finally {
            if (context != null) context.stop();
        }
    }
}
