第三步：实现请求的客户端，该客户端是接收对外的请求的，该模块依赖于如下两个模块：
                  <!-- 需要依赖api模块：该模块里面是接口的声明和实体类，用于给业务客户端和服务端依赖的，这里面是公共的接口和实体类 -->
					<dependency>
						<groupId>com.zzg</groupId>
						<artifactId>zzg-pro-user-api</artifactId>
						<version>0.0.1-SNAPSHOT</version>
					</dependency>
					<!-- 需要把客户端也依赖过来：这个里面是封装了netty的客户端，用于向服务端发请求的 -->
					<dependency>
						<groupId>com.zzg</groupId>
						<artifactId>zzg-pro-netty-consumer</artifactId>
						<version>0.0.1-SNAPSHOT</version>
					</dependency>