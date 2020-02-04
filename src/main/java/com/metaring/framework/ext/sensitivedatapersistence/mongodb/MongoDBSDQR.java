/**
 *    Copyright 2019 MetaRing s.r.l.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.metaring.framework.ext.sensitivedatapersistence.mongodb;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import com.metaring.framework.Tools;
import com.metaring.framework.sensitiveDataPersistence.SensitiveDataInfo;
import com.metaring.framework.sensitiveDataPersistence.SensitiveDataQueryResolver;
import com.metaring.framework.type.DataRepresentation;
import com.metaring.framework.type.series.TextSeries;

import static com.ea.async.Async.await;
import static com.metaring.framework.sensitiveDataPersistence.SensitiveDataPersistenceFunctionalitiesManager.tarnishWithParams;
import static java.util.concurrent.CompletableFuture.completedFuture;

public final class MongoDBSDQR implements SensitiveDataQueryResolver {

    @Override
    public final CompletableFuture<String> resolve(String query, TextSeries params, Function<String, String> trasformFunction) {
        boolean semicolon = query.endsWith(";");
        if(semicolon) {
            query = query.substring(0, query.length() - 1);
        }
        String toFirstBraceSplit = query.substring(0, query.indexOf("("));
        String jsonQuery = "[" + query.substring(query.indexOf("(") + 1, query.lastIndexOf(")")) + "]";
        DataRepresentation dataFromQuery = Tools.FACTORY_DATA_REPRESENTATION.fromJson(jsonQuery);
        DataRepresentation blurredData = await(tarnishWithParams(SensitiveDataInfo.create(dataFromQuery, params)));
        String blurredDataJson = blurredData.toJson();
        blurredDataJson = blurredDataJson.substring(1, blurredDataJson.length() - 1);
        String transformedQuery = toFirstBraceSplit + "(" + blurredDataJson + ")" + (semicolon ? ";" : "");
        return completedFuture(transformedQuery);
    }
}