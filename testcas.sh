#!/bin/bash

clear

printHelp() {
    echo -e "\nUsage: ./testcas.sh --category [category1,category2,...] [--help] [--test TestClass] [--ignore-failures] [--no-wrapper] [--no-retry] [--debug] [--no-parallel] [--dry-run] [--info] \n"
    echo -e "Available test categories are:\n"
    echo -e "simple,memcached,cassandra,groovy,kafka,ldap,rest,mfa,jdbc,mssql,oracle,radius,couchdb,\
mariadb,files,postgres,dynamodb,couchbase,uma,saml,mail,aws,jms,hazelcast,jmx,ehcache,\
oauth,oidc,redis,webflow,mongo,ignite,influxdb,zookeeper,mysql,x509,shell,cosmosdb,config"
    echo -e "\nPlease see the test script for details.\n"
}

parallel="--parallel "
dryRun=""
info=""
gradleCmd="./gradlew"
flags="--no-daemon --configure-on-demand --build-cache -x javadoc -x check -DskipNestedConfigMetadataGen=true -DshowStandardStreams=true "

while (( "$#" )); do
    case "$1" in
    --no-parallel)
        parallel="--no-parallel "
        shift
        ;;
    --info)
        info="--info "
        shift
        ;;
    --dry-run)
        dryRun="--dry-run "
        shift
        ;;
    --no-wrapper)
        gradleCmd="gradle"
        shift
        ;;
    --help)
        printHelp
        exit 0
        ;;
    --debug)
        debug="--debug-jvm "
        parallel=""
        shift
        ;;
    --test)
        tests="--tests \"$2\" "
        shift 2
        ;;
    --no-retry)
        flags+=" -DskipTestRetry=true"
        shift
        ;;
    --ignore-failures)
        flags+=" -DignoreTestFailures=true"
        shift
        ;;
    --category)
        category="$2"
        for item in $(echo "$category" | sed "s/,/ /g")
        do
            case "${item}" in
            test|simple|run|basic|unit|unittests)
                task+="testSimple "
                ;;
            memcached|memcache|kryo)
                ./ci/tests/memcached/run-memcached-server.sh
                task+="testMemcached "
                ;;
            x509)
                task+="testX509 "
                ;;
            shell)
                task+="testSHELL "
                ;;
            uma)
                task+="testUma "
                ;;
            filesystem|files|file|fsys)
                task+="testFileSystem "
                ;;
            config|casconfig|ccfg|cfg)
                task+="testCasConfiguration "
                ;;
            groovy|script)
                task+="testGroovy "
                ;;
            jmx|jmx)
                task+="testJMX "
                ;;
            hz|hazelcast)
                task+="testHazelcast "
                ;;
            mssql)
                ./ci/tests/mssqlserver/run-mssql-server.sh
                task+="testMsSqlServer "
                ;;
            ignite)
                task+="testIgnite "
                ;;
            influx|influxdb)
                ./ci/tests/influxdb/run-influxdb-server.sh
                task+="testInfluxDb "
                ;;
            cosmosdb|cosmos)
                task+="testCosmosDb "
                ;;
            ehcache)
                ./ci/tests/ehcache/run-terracotta-server.sh
                task+="testEhcache "
                ;;
            ldap|ad|activedirectory)
                ./ci/tests/ldap/run-ldap-server.sh
                ./ci/tests/ldap/run-ad-server.sh true
                task+="testLdap "
                ;;
            couchbase)
                ./ci/tests/couchbase/run-couchbase-server.sh
                task+="testCouchbase "
                ;;
            mongo|mongodb)
                ./ci/tests/mongodb/run-mongodb-server.sh
                task+="testMongoDb "
                ;;
            couchdb)
                ./ci/tests/couchdb/run-couchdb-server.sh
                task+="testCouchDb "
                ;;
            rest|restful|restapi)
                task+="testRestfulApi "
                ;;
            mysql)
                ./ci/tests/mysql/run-mysql-server.sh
                task+="testMySQL "
                ;;
            maria|mariadb)
                ./ci/tests/mariadb/run-mariadb-server.sh
                task+="testMariaDb "
                ;;
            jdbc|jpa|database|db|hibernate|rdbms|hsql)
                task+="testJDBC "
                ;;
            postgres|pg|postgresql)
                ./ci/tests/postgres/run-postgres-server.sh
                task+="testPostgres "
                ;;
            cassandra)
                ./ci/tests/cassandra/run-cassandra-server.sh
                task+="testCassandra "
                ;;
            kafka)
                task+="testKafka "
                ;;
            oauth)
                task+="testOAuth "
                ;;
            aws)
                ./ci/tests/aws/run-aws-server.sh
                task+="testAmazonWebServices "
                ;;
            oidc)
                task+="testOIDC "
                ;;
            mfa|duo|gauth|webauthn|authy|fido|u2f|swivel|acceptto)
                task+="testMFA "
                ;;
            saml|saml2)
                task+="testSAML "
                ;;
            radius)
                ./ci/tests/radius/run-radius-server.sh
                task+="testRadius "
                ;;
            mail|email)
                ./ci/tests/mail/run-mail-server.sh
                task+="testMail "
                ;;
            zoo|zookeeper)
                ./ci/tests/zookeeper/run-zookeeper-server.sh
                task+="testZooKeeper "
                ;;
            dynamodb|dynamo)
                ./ci/tests/dynamodb/run-dynamodb-server.sh
                task+="testDynamoDb "
                ;;
            webflow|swf)
                task+="testWebflow "
                ;;
            oracle)
                ./ci/tests/oracle/run-oracle-server.sh
                task+="testOracle "
                ;;
            redis)
                ./ci/tests/redis/run-redis-server.sh
                task+="testRedis "
                ;;
            activemq|amq|jms)
                ./ci/tests/activemq/run-activemq-server.sh
                task+="testJMS "
                ;;
            simple|unit)
                task+="testSimple "
                ;;
            esac
        done
        shift 2
        ;;
    *)
        echo -e "Unable to accept parameter: $1"
        printHelp
        exit 1
        ;;
    esac
done

if [[ -z "$task" ]]
then
  printHelp
  exit 1
fi

cmdstring="\033[1m$gradleCmd \e[32m$task\e[39m$tests\e[39m $flags ${debug}${dryRun}${info}${parallel}\e[39m"
printf "$cmdstring \e[0m\n"

cmd="$gradleCmd $task $tests $flags ${debug} ${parallel} ${dryRun} ${info}"
eval "$cmd"
retVal=$?
echo -e "***************************************************************************************"
echo -e "Gradle build finished at `date` with exit code $retVal"
echo -e "***************************************************************************************"

if [ $retVal == 0 ]; then
    echo "Gradle build finished successfully."
else
    echo "Gradle build did NOT finish successfully."
    exit $retVal
fi
