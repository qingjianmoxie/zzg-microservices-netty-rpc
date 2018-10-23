第一步：这个是项目的服务端，启动类是com.zzg.server.SpringServer 这个，
              该服务端依赖于如下两个模块：
                  <!-- 需要依赖api模块：这是所有的接口声明和实体类放这个公共API模块中 -->	
						<dependency>
							<groupId>com.zzg</groupId>
							<artifactId>zzg-pro-user-api</artifactId>
							<version>0.0.1-SNAPSHOT</version>
						</dependency>
						
						<!-- 需要依赖对外提供服务的模块：负责neetty框架底层的请求处理，通过代理模式执行本工程的业务方法 -->
						<dependency>
							<groupId>com.zzg</groupId>
							<artifactId>zzg-pro-netty-rpc</artifactId>
							<version>0.0.1-SNAPSHOT</version>
						</dependency>
第二步：zzg-pro-user-api这个api工程是对外提供的一个api接口，所有的接口声明和实体类都放这里面，是给服务端和客户端依赖的

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
