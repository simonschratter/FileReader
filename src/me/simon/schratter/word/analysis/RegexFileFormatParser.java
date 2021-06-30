package me.simon.schratter.word.analysis;

public class RegexFileFormatParser implements FileFormatParser {

    private final String regex;

    public RegexFileFormatParser(String regex) {
        this.regex = regex;
    }

    @Override
    public String[] parseLine(String line) {
        return line.trim().split(regex);
    }
}
