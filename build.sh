#!/bin/bash
cd ./build
native-image \
-J-Dsun.nio.ch.maxUpdateArraySize=100 \
-J-DCoordinatorEnvironmentBean.transactionStatusManagerEnable=false \
-J-Dvertx.logger-delegate-factory-class-name=io.quarkus.vertx.core.runtime.VertxLogDelegateFactory \
-J-Dvertx.disableDnsResolver=true \
-J-Dio.netty.leakDetection.level=DISABLED \
-J-Dio.netty.allocator.maxOrder=3 \
-J-Duser.language=vi \
-J-Duser.country=VN \
-J-Dfile.encoding=UTF-8 \
-H:-ParseOnce \
-J--add-exports=java.security.jgss/sun.security.krb5=ALL-UNNAMED \
-J--add-opens=java.base/java.text=ALL-UNNAMED \
--gc=G1 \
-H:InitialCollectionPolicy=com.oracle.svm.core.genscavenge.CollectionPolicy\$BySpaceAndTime \
-H:+JNI \
-H:+AllowFoldMethods \
-J-Djava.awt.headless=true \
-H:FallbackThreshold=0 \
-H:+ReportExceptionStackTraces \
-H:-AddAllCharsets \
-H:EnableURLProtocols=http,https \
-H:NativeLinkerOption=-no-pie \
-H:-UseServiceLoaderFeature \
-H:+StackTrace java-native-1.0-SNAPSHOT-all \
-jar ./libs/java-native-1.0-SNAPSHOT-all.jar
