package org.commonmark.ext.gfm.tables;

import org.commonmark.Extension;
import org.commonmark.node.Node;
import org.commonmark.node.SourceSpan;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.AttributeProvider;
import org.commonmark.renderer.html.AttributeProviderContext;
import org.commonmark.renderer.html.AttributeProviderFactory;
import org.commonmark.renderer.html.HtmlRenderer;
import org.commonmark.test.RenderingTestCase;
import org.junit.Test;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.emptyIterable;
import static org.junit.Assert.assertThat;

public class TablesTest extends RenderingTestCase {

    private static final Set<Extension> EXTENSIONS = Collections.singleton(TablesExtension.create());
    private static final Parser PARSER = Parser.builder().extensions(EXTENSIONS).build();
    private static final HtmlRenderer RENDERER = HtmlRenderer.builder().extensions(EXTENSIONS).build();

    @Test
    public void mustHaveHeaderAndSeparator() {
        assertRendering("Abc|Def", "<p>Abc|Def</p>\n");
        assertRendering("Abc | Def", "<p>Abc | Def</p>\n");
    }

    @Test
    public void separatorMustBeThreeOrMore() {
        assertRendering("Abc|Def\n-|-", "<p>Abc|Def\n-|-</p>\n");
        assertRendering("Abc|Def\n--|--", "<p>Abc|Def\n--|--</p>\n");
    }

    @Test
    public void separatorCanNotHaveLeadingSpaceThenPipe() {
        assertRendering("Abc|Def\n |---|---", "<p>Abc|Def\n|---|---</p>\n");
    }

    @Test
    public void headerMustBeOneLine() {
        assertRendering("No\nAbc|Def\n---|---", "<p>No\nAbc|Def\n---|---</p>\n");
    }

    @Test
    public void oneHeadNoBody() {
        assertRendering("Abc|Def\n---|---", "<table>\n" +
                "<thead>\n" +
                "<tr><th>Abc</th><th>Def</th></tr>\n" +
                "</thead>\n" +
                "<tbody></tbody>\n" +
                "</table>\n");
    }

    @Test
    public void oneColumnOneHeadNoBody() {
        String expected = "<table>\n" +
                "<thead>\n" +
                "<tr><th>Abc</th></tr>\n" +
                "</thead>\n" +
                "<tbody></tbody>\n" +
                "</table>\n";
        assertRendering("|Abc\n|---\n", expected);
        assertRendering("|Abc|\n|---|\n", expected);
        assertRendering("Abc|\n---|\n", expected);

        // Pipe required on separator
        assertRendering("|Abc\n---\n", "<h2>|Abc</h2>\n");
        // Pipe required on head
        assertRendering("Abc\n|---\n", "<p>Abc\n|---</p>\n");
    }

    @Test
    public void oneColumnOneHeadOneBody() {
        String expected = "<table>\n" +
                "<thead>\n" +
                "<tr><th>Abc</th></tr>\n" +
                "</thead>\n" +
                "<tbody>\n" +
                "<tr><td>1</td></tr>\n" +
                "</tbody>\n" +
                "</table>\n";
        assertRendering("|Abc\n|---\n|1", expected);
        assertRendering("|Abc|\n|---|\n|1|", expected);
        assertRendering("Abc|\n---|\n1|", expected);

        // Pipe required on separator
        assertRendering("|Abc\n---\n|1", "<h2>|Abc</h2>\n<p>|1</p>\n");

        // Pipe required on body
        assertRendering("|Abc\n|---\n1\n", "<table>\n" +
                "<thead>\n" +
                "<tr><th>Abc</th></tr>\n" +
                "</thead>\n" +
                "<tbody></tbody>\n" +
                "</table>\n" +
                "<p>1</p>\n");
    }

    @Test
    public void oneHeadOneBody() {
        assertRendering("Abc|Def\n---|---\n1|2", "<table>\n" +
                "<thead>\n" +
                "<tr><th>Abc</th><th>Def</th></tr>\n" +
                "</thead>\n" +
                "<tbody>\n" +
                "<tr><td>1</td><td>2</td></tr>\n" +
                "</tbody>\n" +
                "</table>\n");
    }

    @Test
    public void separatorMustNotHaveLessPartsThanHead() {
        assertRendering("Abc|Def|Ghi\n---|---\n1|2|3", "<p>Abc|Def|Ghi\n---|---\n1|2|3</p>\n");
    }

    @Test
    public void padding() {
        assertRendering(" Abc  | Def \n --- | --- \n 1 | 2 ", "<table>\n" +
                "<thead>\n" +
                "<tr><th>Abc</th><th>Def</th></tr>\n" +
                "</thead>\n" +
                "<tbody>\n" +
                "<tr><td>1</td><td>2</td></tr>\n" +
                "</tbody>\n" +
                "</table>\n");
    }

    @Test
    public void paddingWithCodeBlockIndentation() {
        assertRendering("Abc|Def\n---|---\n    1|2", "<table>\n" +
                "<thead>\n" +
                "<tr><th>Abc</th><th>Def</th></tr>\n" +
                "</thead>\n" +
                "<tbody>\n" +
                "<tr><td>1</td><td>2</td></tr>\n" +
                "</tbody>\n" +
                "</table>\n");
    }

    @Test
    public void pipesOnOutside() {
        assertRendering("|Abc|Def|\n|---|---|\n|1|2|", "<table>\n" +
                "<thead>\n" +
                "<tr><th>Abc</th><th>Def</th></tr>\n" +
                "</thead>\n" +
                "<tbody>\n" +
                "<tr><td>1</td><td>2</td></tr>\n" +
                "</tbody>\n" +
                "</table>\n");
    }

    @Test
    public void inlineElements() {
        assertRendering("*Abc*|Def\n---|---\n1|2", "<table>\n" +
                "<thead>\n" +
                "<tr><th><em>Abc</em></th><th>Def</th></tr>\n" +
                "</thead>\n" +
                "<tbody>\n" +
                "<tr><td>1</td><td>2</td></tr>\n" +
                "</tbody>\n" +
                "</table>\n");
    }

    @Test
    public void escapedPipe() {
        assertRendering("Abc|Def\n---|---\n1\\|2|20", "<table>\n" +
                "<thead>\n" +
                "<tr><th>Abc</th><th>Def</th></tr>\n" +
                "</thead>\n" +
                "<tbody>\n" +
                "<tr><td>1|2</td><td>20</td></tr>\n" +
                "</tbody>\n" +
                "</table>\n");
    }

    @Test
    public void escapedBackslash() {
        assertRendering("Abc|Def\n---|---\n1\\\\|2", "<table>\n" +
                "<thead>\n" +
                "<tr><th>Abc</th><th>Def</th></tr>\n" +
                "</thead>\n" +
                "<tbody>\n" +
                "<tr><td>1\\</td><td>2</td></tr>\n" +
                "</tbody>\n" +
                "</table>\n");
    }

    @Test
    public void alignLeft() {
        assertRendering("Abc|Def\n:---|---\n1|2", "<table>\n" +
                "<thead>\n" +
                "<tr><th align=\"left\">Abc</th><th>Def</th></tr>\n" +
                "</thead>\n" +
                "<tbody>\n" +
                "<tr><td align=\"left\">1</td><td>2</td></tr>\n" +
                "</tbody>\n" +
                "</table>\n");
    }

    @Test
    public void alignRight() {
        assertRendering("Abc|Def\n---:|---\n1|2", "<table>\n" +
                "<thead>\n" +
                "<tr><th align=\"right\">Abc</th><th>Def</th></tr>\n" +
                "</thead>\n" +
                "<tbody>\n" +
                "<tr><td align=\"right\">1</td><td>2</td></tr>\n" +
                "</tbody>\n" +
                "</table>\n");
    }

    @Test
    public void alignCenter() {
        assertRendering("Abc|Def\n:---:|---\n1|2", "<table>\n" +
                "<thead>\n" +
                "<tr><th align=\"center\">Abc</th><th>Def</th></tr>\n" +
                "</thead>\n" +
                "<tbody>\n" +
                "<tr><td align=\"center\">1</td><td>2</td></tr>\n" +
                "</tbody>\n" +
                "</table>\n");
    }

    @Test
    public void alignCenterSecond() {
        assertRendering("Abc|Def\n---|:---:\n1|2", "<table>\n" +
                "<thead>\n" +
                "<tr><th>Abc</th><th align=\"center\">Def</th></tr>\n" +
                "</thead>\n" +
                "<tbody>\n" +
                "<tr><td>1</td><td align=\"center\">2</td></tr>\n" +
                "</tbody>\n" +
                "</table>\n");
    }

    @Test
    public void alignLeftWithSpaces() {
        assertRendering("Abc|Def\n :--- |---\n1|2", "<table>\n" +
                "<thead>\n" +
                "<tr><th align=\"left\">Abc</th><th>Def</th></tr>\n" +
                "</thead>\n" +
                "<tbody>\n" +
                "<tr><td align=\"left\">1</td><td>2</td></tr>\n" +
                "</tbody>\n" +
                "</table>\n");
    }

    @Test
    public void alignmentMarkerMustBeNextToDashes() {
        assertRendering("Abc|Def\n: ---|---", "<p>Abc|Def\n: ---|---</p>\n");
        assertRendering("Abc|Def\n--- :|---", "<p>Abc|Def\n--- :|---</p>\n");
        assertRendering("Abc|Def\n---|: ---", "<p>Abc|Def\n---|: ---</p>\n");
        assertRendering("Abc|Def\n---|--- :", "<p>Abc|Def\n---|--- :</p>\n");
    }

    @Test
    public void bodyCanNotHaveMoreColumnsThanHead() {
        assertRendering("Abc|Def\n---|---\n1|2|3", "<table>\n" +
                "<thead>\n" +
                "<tr><th>Abc</th><th>Def</th></tr>\n" +
                "</thead>\n" +
                "<tbody>\n" +
                "<tr><td>1</td><td>2</td></tr>\n" +
                "</tbody>\n" +
                "</table>\n");
    }

    @Test
    public void bodyWithFewerColumnsThanHeadResultsInEmptyCells() {
        assertRendering("Abc|Def|Ghi\n---|---|---\n1|2", "<table>\n" +
                "<thead>\n" +
                "<tr><th>Abc</th><th>Def</th><th>Ghi</th></tr>\n" +
                "</thead>\n" +
                "<tbody>\n" +
                "<tr><td>1</td><td>2</td><td></td></tr>\n" +
                "</tbody>\n" +
                "</table>\n");
    }

    @Test
    public void insideBlockQuote() {
        assertRendering("> Abc|Def\n> ---|---\n> 1|2", "<blockquote>\n" +
                "<table>\n" +
                "<thead>\n" +
                "<tr><th>Abc</th><th>Def</th></tr>\n" +
                "</thead>\n" +
                "<tbody>\n" +
                "<tr><td>1</td><td>2</td></tr>\n" +
                "</tbody>\n" +
                "</table>\n" +
                "</blockquote>\n");
    }

    @Test
    public void tableEndWithoutEmptyLine() {
        assertRendering("Abc|Def\n---|---\n1|2\ntable, you are over", "<table>\n" +
                "<thead>\n" +
                "<tr><th>Abc</th><th>Def</th></tr>\n" +
                "</thead>\n" +
                "<tbody>\n" +
                "<tr><td>1</td><td>2</td></tr>\n" +
                "</tbody>\n" +
                "</table>\n" +
                "<p>table, you are over</p>\n");
    }

    @Test
    public void attributeProviderIsApplied() {
        AttributeProviderFactory factory = new AttributeProviderFactory() {
            @Override
            public AttributeProvider create(AttributeProviderContext context) {
                return new AttributeProvider() {
                    @Override
                    public void setAttributes(Node node, String tagName, Map<String, String> attributes) {
                        if (node instanceof TableBlock) {
                            attributes.put("test", "block");
                        } else if (node instanceof TableHead) {
                            attributes.put("test", "head");
                        } else if (node instanceof TableBody) {
                            attributes.put("test", "body");
                        } else if (node instanceof TableRow) {
                            attributes.put("test", "row");
                        } else if (node instanceof TableCell) {
                            attributes.put("test", "cell");
                        }
                    }
                };
            }
        };
        HtmlRenderer renderer = HtmlRenderer.builder()
                .attributeProviderFactory(factory)
                .extensions(EXTENSIONS)
                .build();
        String rendered = renderer.render(PARSER.parse("Abc|Def\n---|---\n1|2"));
        assertThat(rendered, is("<table test=\"block\">\n" +
                "<thead test=\"head\">\n" +
                "<tr test=\"row\"><th test=\"cell\">Abc</th><th test=\"cell\">Def</th></tr>\n" +
                "</thead>\n" +
                "<tbody test=\"body\">\n" +
                "<tr test=\"row\"><td test=\"cell\">1</td><td test=\"cell\">2</td></tr>\n" +
                "</tbody>\n" +
                "</table>\n"));
    }

    @Test
    public void sourceSpans() {
        Node document = PARSER.parse("Abc|Def\n---|---\n|1|2\n 3|four|\n|||\n");

        TableBlock block = (TableBlock) document.getFirstChild();
        assertThat(block.getSourceSpans(), contains(SourceSpan.of(0, 0, 7), SourceSpan.of(1, 0, 7),
                SourceSpan.of(2, 0, 4), SourceSpan.of(3, 1, 7), SourceSpan.of(4, 0, 3)));

        TableHead head = (TableHead) block.getFirstChild();
        assertThat(head.getSourceSpans(), contains(SourceSpan.of(0, 0, 7)));

        TableRow headRow = (TableRow) head.getFirstChild();
        assertThat(headRow.getSourceSpans(), contains(SourceSpan.of(0, 0, 7)));
        TableCell headRowCell1 = (TableCell) headRow.getFirstChild();
        TableCell headRowCell2 = (TableCell) headRow.getLastChild();
        assertThat(headRowCell1.getSourceSpans(), contains(SourceSpan.of(0, 0, 3)));
        assertThat(headRowCell2.getSourceSpans(), contains(SourceSpan.of(0, 4, 3)));

        TableBody body = (TableBody) block.getLastChild();
        assertThat(body.getSourceSpans(), contains(SourceSpan.of(2, 0, 4), SourceSpan.of(3, 1, 7), SourceSpan.of(4, 0, 3)));

        TableRow bodyRow1 = (TableRow) body.getFirstChild();
        assertThat(bodyRow1.getSourceSpans(), contains(SourceSpan.of(2, 0, 4)));
        TableCell bodyRow1Cell1 = (TableCell) bodyRow1.getFirstChild();
        TableCell bodyRow1Cell2 = (TableCell) bodyRow1.getLastChild();
        assertThat(bodyRow1Cell1.getSourceSpans(), contains(SourceSpan.of(2, 1, 1)));
        assertThat(bodyRow1Cell2.getSourceSpans(), contains(SourceSpan.of(2, 3, 1)));

        TableRow bodyRow2 = (TableRow) body.getFirstChild().getNext();
        assertThat(bodyRow2.getSourceSpans(), contains(SourceSpan.of(3, 1, 7)));
        TableCell bodyRow2Cell1 = (TableCell) bodyRow2.getFirstChild();
        TableCell bodyRow2Cell2 = (TableCell) bodyRow2.getLastChild();
        assertThat(bodyRow2Cell1.getSourceSpans(), contains(SourceSpan.of(3, 1, 1)));
        assertThat(bodyRow2Cell2.getSourceSpans(), contains(SourceSpan.of(3, 3, 4)));

        TableRow bodyRow3 = (TableRow) body.getLastChild();
        assertThat(bodyRow3.getSourceSpans(), contains(SourceSpan.of(4, 0, 3)));
        TableCell bodyRow3Cell1 = (TableCell) bodyRow3.getFirstChild();
        TableCell bodyRow3Cell2 = (TableCell) bodyRow3.getLastChild();
        assertThat(bodyRow3Cell1.getSourceSpans(), emptyIterable());
        assertThat(bodyRow3Cell2.getSourceSpans(), emptyIterable());
    }

    @Override
    protected String render(String source) {
        return RENDERER.render(PARSER.parse(source));
    }
}
