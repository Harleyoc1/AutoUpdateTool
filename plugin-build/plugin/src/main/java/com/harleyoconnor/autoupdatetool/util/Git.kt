package com.harleyoconnor.autoupdatetool.util

import java.io.File
import java.util.stream.Stream

fun getLastTag(workingDir: File): String {
    return executeGitCommand(listOf("describe", "--tags", "--abbrev=0"), workingDir).trim()
}

fun getCommitsSince(version: String, workingDir: File): Stream<Commit> {
    return executeGitCommand(listOf("log", "$version..HEAD", "--oneline"), workingDir).split("\n")
        .stream()
        .filter { it.isNotBlank() }
        .map { commitData -> commitData.substring(0, commitData.indexOf(' ')) }
        .map { hash -> getCommit(hash, workingDir) }
}

fun getCommit(hash: String, workingDir: File): Commit {
    val commitData = executeGitCommand(listOf("show", hash), workingDir).split("\n").toMutableList()
    if (commitData[1].startsWith("Merge")) {
        commitData.removeAt(1)
    }
    val authorName = getCommitAuthorName(commitData[1])
    val message = commitData[4].trim()
    return Commit(hash, authorName, message)
}

private fun getCommitAuthorName(authorData: String): String {
    val startOfEmail = authorData.indexOf("<")
    val endIndex = if (startOfEmail < 0) authorData.length else startOfEmail - 1
    return authorData.substring(authorData.indexOf(":") + 2, endIndex)
}

/**
 * @return standard output from the process
 */
private fun executeGitCommand(command: List<String>, workingDir: File): String {
    try {
        val process = ProcessBuilder().command(command.appendToHead("git")).directory(workingDir).start()
        val out = process.inputStream.reader().readText()
        process.waitFor()
        if (process.exitValue() != 0) {
            error(process.errorStream.reader().readText())
        }
        return out
    } catch (e: Exception) {
        error("Exception while executing Git command `$command`: " + e.message.toString())
    }
}

fun <E> List<E>.appendToHead(element: E): List<E> {
    val mutable = this.toMutableList()
    mutable.add(0, element)
    return mutable
}

data class Commit(
    val hash: String,
    val authorName: String,
    val message: String
)
