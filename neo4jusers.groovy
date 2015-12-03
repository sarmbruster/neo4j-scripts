#!/usr/bin/env groovy

@Grab(group="org.neo4j.app", module="neo4j-server", version="2.3.1")

import org.neo4j.logging.NullLogProvider
import org.neo4j.server.security.auth.FileUserRepository
import org.neo4j.server.security.auth.Credential
import org.neo4j.server.security.auth.User

def cli = new CliBuilder(usage: 'neo4jusers.groovy -f file [-a user:pass] [-u user:pass] [-d user] [-l]')
cli.with {
    h longOpt: 'help', 'Show usage information'
    f args: 1, required: true, "location of neo4j's auth file, normally in data/dbms/auth"
    a args: 1, 'add a user, specify username and password'
    u args: 1, "change a user's password"
    d args: 1, "delete a user"
    l "list users"
}
    
def options = cli.parse(args)
if (!options) {
    return
}
if (options.h) {
    cli.usage()
    return
}


def repo = new FileUserRepository(new File(options.f).toPath(), new NullLogProvider())
repo.loadUsersFromFile()

if (options.l) {
    repo.usersByName.each {k,v -> println k}
}

if (options.a) {
    def (username, pw) = options.a.split(/:/)
    println "adding user $username $pw"
    def user = new User(username, Credential.forPassword(pw), false)
    repo.create(user)
}

if (options.d) {
    def username = options.d
    def user = repo.findByName(options.d)
    if (!user) {
        throw new RuntimeException("cannot find user ${username} in ${options.f}")
    }
    repo.delete(user)
    println "deleted user $username"
}

if (options.u) {
    def (username, pw) = options.a.split(/:/)
    def user = repo.findByName(username)
    if (!user) {
        throw new RuntimeException("cannot find user ${options.d} in ${options.f}")
    }
    repo.update( user, new User(username, Credential.forPassword(pw), false))
    println "changed password for user $username"
}

repo.saveUsersToFile()