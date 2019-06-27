## Copyright 2015 JAXIO http://www.jaxio.com
##
## Licensed under the Apache License, Version 2.0 (the "License");
## you may not use this file except in compliance with the License.
## You may obtain a copy of the License at
##
##    http://www.apache.org/licenses/LICENSE-2.0
##
## Unless required by applicable law or agreed to in writing, software
## distributed under the License is distributed on an "AS IS" BASIS,
## WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
## See the License for the specific language governing permissions and
## limitations under the License.
##
$output.generateIf($project.isAccountEntityPresent())##
$output.skipIf($project.accountEntity.isVirtual())##
$output.javaTest($RepositorySupport, "ByNamedQueryUtilIT")

$output.require("javax.inject.Inject")##
$output.require("${configuration.rootPackage}.jaxio.commons.*")##
$output.require("org.junit.Test")##
$output.require("org.junit.runner.RunWith")##
$output.requireStatic("org.fest.assertions.Assertions.assertThat")##
$output.require("org.springframework.boot.test.context.SpringBootTest")##
$output.require("org.springframework.context.annotation.ComponentScan")##
$output.require("org.springframework.test.annotation.Rollback")##
$output.require("org.springframework.test.context.junit4.SpringRunner")##
$output.require("org.springframework.transaction.annotation.Transactional")##


@SpringBootTest(classes=${output.currentClass}.class)
@RunWith(SpringRunner.class)
@ComponentScan(value = {"${configuration.rootPackage}"})
public class $output.currentClass {

    @Inject
    private ByNamedQueryUtil byNamedQueryUtil;

#if (!$project.accountEntity.isVirtual())
    @Test
    public void allAccountsUsingNamedQuery() {
        SearchParameters searchParameters = new SearchParameters().namedQuery("$project.accountEntity.jpa.selectAllNamedQuery");
		assertThat(byNamedQueryUtil.findByNamedQuery(searchParameters)).hasSize(#if($project.isDefaultSchema())53#{else}1#{end});
    }

    @Test
    public void allAccountsUsingNativeNamedQuery() {
        SearchParameters searchParameters = new SearchParameters().namedQuery("$project.accountEntity.jpa.selectAllNativeNamedQuery");
		assertThat(byNamedQueryUtil.findByNamedQuery(searchParameters)).hasSize(#if($project.isDefaultSchema())53#{else}1#{end});
    }
#else
    @Test
    public void createAccountTableToSeeNamedQueryTests() {
        log.info("Create an account table to see generated named query utils");
    }
#end
}