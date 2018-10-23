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
		
		