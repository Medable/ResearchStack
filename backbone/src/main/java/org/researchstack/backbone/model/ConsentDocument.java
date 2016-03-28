package org.researchstack.backbone.model;
import android.support.annotation.StringRes;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * Initializer with ORKHTMLPDFWriter parameter. Allows for injecting mock dependency for the
 * purposes of isolated unit testing.
 *
 * @param writer             The instance of the ORKHTMLPDFWriter upon which the class depends.
 * @param sectionFormatter   An instance of ORKConsentSectionFormatter
 * @param signatureFormatter An instance of ORKConsentSignatureFormatter
 *                           <p>
 *                           public void initWithHTMLPDFWriter(HTMLPDFWriter writer, ConsentSectionFormatter sectionFormatter,
 *                           SignatureFormatter signatureFormatter) { }
 *                           <p>
 *                           <p>
 *                           The PDF is generated in a form suitable for printing. This is done asynchronously,
 *                           so the PDF data is returned through a completion block.
 * @param handler            The handler block for generated PDF data. When successful, the returned
 *                           data represents a complete PDF document that represents the consent.
 *                           <p>
 *                           - (void)makePDFWithCompletionHandler:(void (^)(NSData * __nullable PDFData, NSError * __nullable error))handler;
 */
public class ConsentDocument implements Serializable
{

    /**
     * The document's title in a localized string.
     * <p>
     * The title appears only in the generated PDF for review; it is not used in the
     * visual consent process.
     */
    private String title;

    /**
     * The sections to be in printed in the PDF file and or presented in the
     * visual consent sequence.
     * <p>
     * All sections appear in the animated process, except for those sections of the
     * type `ORKConsentSectionTypeOnlyInDocument`.
     * <p>
     * If the `htmlReviewContent` property is not set, this content is also used to
     * populate the document for review in the consent review step.
     * <p>
     * The PDF file contains all sections.
     */
    private List<ConsentSection> sections;

    /**
     * The title to be rendered on the signature page of the generated PDF in a localized string.
     * <p>
     * The title is ignored for visual consent. The title is also ignored if you supply a value for the `htmlReviewContent` property.
     */
    private int signaturePageTitle;

    /**
     * The content to be rendered below the title on the signature page of the generated PDF in a localized string.
     * <p>
     * The content is ignored for visual consent. The content is also ignored if you supply a value for the `htmlReviewContent` property.
     */
    @Deprecated
    private String signaturePageContent;

    /**
     * The set of signatures that are required or prepopulated in the document.
     * <p>
     * To add a signature to the document after consent review, the `signatures` array
     * needs to be modified to incorporate the new signature content prior to PDF
     * generation. For more information, see `[ORKConsentSignatureResult applyToDocument:]`.
     */
    private List<ConsentSignature> signatures = new ArrayList<>(1);

    /**
     * Override HTML content for review.
     * <p>
     * Typically, the review content is generated from the values of the `sections` and `signatures`
     * properties.
     * <p>
     * When this property is set, the review content is reproduced exactly as provided in the property
     * in the consent review step, and the `sections` and `signatures` properties
     * are ignored.
     */
    private String htmlReviewContent;


    public void setTitle(String title)
    {
        this.title = title;
    }

    @StringRes
    public int getSignaturePageTitle()
    {
        return signaturePageTitle;
    }

    public void setSignaturePageTitle(@StringRes int signaturePageTitle)
    {
        this.signaturePageTitle = signaturePageTitle;
    }

    @Deprecated
    public void setSignaturePageContent(String signaturePageContent)
    {
        this.signaturePageContent = signaturePageContent;
    }

    /**
     * Adds a signature to the array of signatures.
     *
     * @param signature The signature object to add to the document.
     */
    public void addSignature(ConsentSignature signature)
    {
        signatures.add(signature);
    }

    public ConsentSignature getSignature(int location)
    {
        return signatures.get(location);
    }

    public List<ConsentSection> getSections()
    {
        return sections;
    }

    public void setSections(List<ConsentSection> sections)
    {
        this.sections = sections;
    }

    public String getHtmlReviewContent()
    {
        return htmlReviewContent;
    }

    public void setHtmlReviewContent(String htmlReviewContent)
    {
        this.htmlReviewContent = htmlReviewContent;
    }

}