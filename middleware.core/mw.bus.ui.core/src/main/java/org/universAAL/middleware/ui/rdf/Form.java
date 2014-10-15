/*	
	Copyright 2007-2014 Fraunhofer IGD, http://www.igd.fraunhofer.de
	Fraunhofer-Gesellschaft - Institute for Computer Graphics Research
	
	See the NOTICE file distributed with this work for additional 
	information regarding copyright ownership
	
	Licensed under the Apache License, Version 2.0 (the "License");
	you may not use this file except in compliance with the License.
	You may obtain a copy of the License at
	
	  http://www.apache.org/licenses/LICENSE-2.0
	
	Unless required by applicable law or agreed to in writing, software
	distributed under the License is distributed on an "AS IS" BASIS,
	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	See the License for the specific language governing permissions and
	limitations under the License.
 */
package org.universAAL.middleware.ui.rdf;

import java.util.List;

import javax.xml.datatype.XMLGregorianCalendar;

import org.universAAL.middleware.owl.ManagedIndividual;
import org.universAAL.middleware.owl.MergedRestriction;
import org.universAAL.middleware.rdf.Resource;
import org.universAAL.middleware.rdf.TypeMapper;
import org.universAAL.middleware.ui.UICaller;
import org.universAAL.middleware.ui.owl.DialogType;

/**
 * Forms can be used to describe dialogs in a modality- & layout-neutral way. A
 * Form is a container of a set of {@link FormControl}s that specify the
 * structure of the dialog as it should be presented to human users. This
 * implementation organizes the {@link FormControl}s in three standard
 * {@link Group}s:
 * <dl>
 * <dt>The submits group</dt>
 * <dd>"buttons" that finish the whole dialog should be added to this group.</dd>
 * <dt>The ioControls group</dt>
 * <dd>all other form controls, no matter if input or output controls, or
 * subgroups or even submits that trigger a sub-dialog should be added to this
 * group.</dd>
 * <dt>The stdButton group</dt>
 * <dd>this group is reserved for a dialog management solution that has access
 * to all dialogs and may be willing to add standard buttons beyond the
 * application logic to reflect a system-wide behavior.</dd>
 * </dl>
 * Forms support four dialog types:
 * <dl>
 * <dt>Message</dt>
 * <dd>The simplest dialog type that can be used to specify a text message to be
 * presented as a notification. No standard buttons in the above sense are
 * allowed. The text message will be used to construct a {@link SimpleOutput}
 * object that is added as the single child to the ioControls group. The submits
 * group will be constructed automatically with two buttons based on the
 * constants {@link #ACK_MESSAGE_DELET} resp. {@link #ACK_MESSAGE_KEEP} and
 * {@link #LABEL_MESSAGE_DELET} resp. {@link #LABEL_MESSAGE_KEEP}. These two
 * buttons can be used by a user to state either
 * "ok, I got it, you can delete the message" or "please preserve the message
 * for later check", respectively. Normally, applications do not need to
 * subscribe for user decision in this regard, but if they want, then they must
 * use the value returned by the method {@link #getDialogID()} of the created
 * form object, for subscribing to the UI bus.</dd>
 * <dt>System Menu</dt>
 * <dd>Reserved for a dialog management solution to present the system main
 * menu. No submits group is allowed in this type of dialog.</dd>
 * <dt>Subdialog</dt>
 * <dd>A dialog related to a running dialog that must be "popped up" when the
 * user presses a button in the originally running dialog without ending that
 * running dialog. No standard buttons in the above sense are allowed.
 * Applications must use instances of {@link SubdialogTrigger} for placing
 * buttons that pop up a subdialog, instead of instances of {@link Submit} that
 * should be used only for finishing the whole dialog. {@link Submit} instances
 * must be added to the group returned by Form.getSubmits(), whereas
 * {@link SubdialogTrigger} instances should be added to appropriate subgroups
 * of the group returned by Form.getIOControls(). Subdialogs may affect the data
 * to be used within the main dialog and hence the main dialog must be frozen
 * until the application requests to continue with it. For this purpose,
 * applications must call the method
 * {@link UICaller#resumeDialog(String,org.universAAL.middleware.rdf.Resource)}
 * of their output publisher after the subdialog finishes and they have
 * processed its data and updated the form data of the main dialog. UI handlers
 * may decide to render instances of {@link SubdialogTrigger} and {@link Submit}
 * differently. Additionally, they may differentiate between events from these
 * two types of buttons (and e.g. keep the parent dialog open until the
 * subdialog loop is closed) or not. In any case, the middleware will
 * re-dispatch the main dialog by calling
 * {@link org.universAAL.middleware.ui.UIHandler#handleUICall(org.universAAL.middleware.ui.UIRequest)}
 * of the output subscriber of the selected UI handler, as soon as the
 * application requests to resume the dialog. In this way, the freezing and
 * re-activating the main dialog is forced by the middleware even if the UI
 * handler does not differentiate between events from instances of
 * {@link SubdialogTrigger} and {@link Submit}.</dd>
 * <dt>Standard Dialog</dt>
 * <dd>All other forms must be constructed with this dialog type. All the three
 * standard groups will be created automatically. Applications can get the
 * standard groups "ioControls" and "submits" by calling the
 * {@link #getIOControls()} and {@link #getSubmits()} methods respectively and
 * construct their dialogs using these two groups as parent for their real form
 * controls.</dd>
 * </dl>
 * 
 * @see <a
 *      href="ftp://ftp.igd.fraunhofer.de/outgoing/mtazari/persona/dialogPackage.jpg">
 *      ftp://ftp.igd.fraunhofer.de/outgoing/mtazari/persona/dialogPackage.jpg</a>
 * @author mtazari
 * @author Carsten Stockloew
 * @navassoc - "IOControlls\n Submits\n StandardButtons" - Group
 * @navassoc - "parentDialog" 1 Form
 */
public class Form extends FormElement {
    public static final String uAAL_DIALOG_NAMESPACE = uAAL_NAMESPACE_PREFIX
	    + "Dialog.owl#";
    public static final String MY_URI = uAAL_DIALOG_NAMESPACE + "Form";

    /**
     * The submission ID if a user acknowledges that a dialog of type Message
     * can be deleted.
     */
    public static final String ACK_MESSAGE_DELET = "deleteMessage";

    /**
     * The submission ID if a user acknowledges that a dialog of type Message
     * must be preserved for later check.
     */
    public static final String ACK_MESSAGE_KEEP = "preserveMessage";

    /**
     * The label of a button associated with {@link #ACK_MESSAGE_DELET} as
     * submission ID that is automatically added to dialogs of type Message.
     */
    public static final Label LABEL_MESSAGE_DELET = new Label("Delete", null);

    /**
     * The label of a button associated with {@link #ACK_MESSAGE_KEEP} as
     * submission ID that is automatically added to dialogs of type Message.
     */
    public static final Label LABEL_MESSAGE_KEEP = new Label("Preserve", null);

    /**
     * An optional property of form objects that can be associated with a string
     * representing the name of the component that created the form.
     */
    public static final String PROP_DIALOG_CREATED_BY = uAAL_DIALOG_NAMESPACE
	    + "createdBy";

    /**
     * The point of time in which a form object is instantiated. This property
     * is set automatically with the value returned by
     * {@link org.universAAL.middleware.rdf.TypeMapper#getCurrentDateTime()}.
     */
    public static final String PROP_DIALOG_CREATION_TIME = uAAL_DIALOG_NAMESPACE
	    + "creationTimestamp";

    /**
     * An optional property of form objects to indicate which form control
     * should receive the focus when UI handlers start to present the dialog. It
     * will be set automatically by the middleware whenever a running dialog is
     * cut so that a seamless resumption of the dialog at a later point in time
     * is guaranteed. UI handlers should check if this property is set. If yes,
     * then they must simulate their logic of presenting the form until they
     * reach the form control given as value of this property. At this point
     * they can prompt the user for the next data entry.
     */
    public static final String PROP_DIALOG_CURRENT_FOCUSED_CONTROL = uAAL_DIALOG_NAMESPACE
	    + "currentFocus";

    /**
     * The {@link org.universAAL.middleware.rdf.Resource} containing the form
     * data. Form data can be accessed using property paths; UI handlers,
     * however, do not need to explicitly access this data normally, because
     * they normally deal only with data associated with form controls that can
     * be retrieved by calling {@link FormControl#getValue()} or set by calling
     * {@link Input#storeUserInput(Object)}. Applications may create an instance
     * of {@link org.universAAL.middleware.rdf.Resource} and set both their
     * hidden data and initial data associated with the form controls using
     * {@link org.universAAL.middleware.rdf.Resource#setPropertyPath(String[], Object)}
     * . They can retrieve the form data from ui responses by calling
     * {@link org.universAAL.middleware.ui.UIResponse#getUserInput(String[])} .
     * Note: initial data to be associated with form controls can be set through
     * their constructors, as well.
     */
    public static final String PROP_DIALOG_DATA_ROOT = uAAL_DIALOG_NAMESPACE
	    + "dialogDataRoot";

    /**
     * The type of the dialog represented by a form object as an instance of
     * {@link DialogType}. See the above documentation of this class regarding
     * the types of dialogs supported.
     */
    public static final String PROP_DIALOG_TYPE = uAAL_DIALOG_NAMESPACE
	    + "dialogType";

    /**
     * Applications must set this property for those form objects representing a
     * dialog of type Subdialog using the value returned by
     * {@link #getDialogID()} on the parent form from which the subdialog was
     * triggered.
     */
    public static final String PROP_PARENT_DIALOG_URI = uAAL_DIALOG_NAMESPACE
	    + "parentDialogURI";

    protected static final String PROP_ROOT_GROUP = uAAL_DIALOG_NAMESPACE
	    + "rootGroup";

    private static final String STD_BUTTONS_DIALOG_ID_SUFFIX = "stdButtons";

    static MergedRestriction getPPathRestriction(String[] pp, Resource pr) {
	if (pp == null || pp.length == 0 || pr == null)
	    return null;
	for (int i = 0; i < pp.length - 1; i++) {
	    Object o = pr.getProperty(pp[i]);
	    if (o == null)
		return getPPathRestriction(pp, i, pr.getType());
	    else if (o instanceof Resource)
		pr = (Resource) o;
	    else
		return null;
	}
	return ManagedIndividual.getClassRestrictionsOnProperty(pr.getType(),
		pp[pp.length - 1]);
    }

    static MergedRestriction getPPathRestriction(String[] pp, int i,
	    String typeURI) {
	if (typeURI == null)
	    return null;

	MergedRestriction r = ManagedIndividual.getClassRestrictionsOnProperty(
		typeURI, pp[i]);
	if (i == pp.length - 1)
	    return r;

	return (r == null) ? null : getPPathRestriction(pp, i + 1,
		r.getPropTypeURI());
    }

    static Object getValue(String[] pp, Resource pr) {
	if (pp == null || pp.length == 0 || pr == null)
	    return null;

	Object o = pr.getProperty(pp[0]);
	for (int i = 1; o != null && i < pp.length; i++) {
	    if (!(o instanceof Resource))
		return null;
	    pr = (Resource) o;
	    o = pr.getProperty(pp[i]);
	}

	return o;
    }

    /**
     * Constructs and returns a new form object representing an empty dialog of
     * type {@link DialogType#stdDialog Standard Dialog} with proper initial
     * configuration. See also the discussion of dialog types in the above
     * documentation of this class.
     * 
     * @param formTitle
     *            The form title giving the intent of the dialog.
     * @param dataRoot
     *            The Resource containing the form data. It can be null, if no
     *            hidden data was prepared by the application and any initial
     *            value to be associated with form controls is going to be set
     *            through their constructors. See also
     *            {@link #PROP_DIALOG_DATA_ROOT}.
     * @return A newly constructed form object representing an empty dialog of
     *         type {@link DialogType#stdDialog Standard Dialog} with proper
     *         initial configuration.
     */
    public static Form newDialog(String formTitle, Resource dataRoot) {
	Form f = new Form(formTitle, dataRoot);
	f.props.put(PROP_DIALOG_TYPE, DialogType.stdDialog);
	Group root = (Group) f.props.get(PROP_ROOT_GROUP);
	new Group(root, new Label(Group.STD_IO_CONTROLS, null), null, null,
		null);
	new Group(root, new Label(Group.STD_SUBMITS, null), null, null, null);
	new Group(root, new Label(Group.STD_STD_BUTTONS, null), null, null,
		null);
	return f;
    }

    /**
     * An alternative for {@link #newDialog(String, Resource)} to be used if no
     * specific data root is going to be specified but only the URI of its type.
     */
    public static Form newDialog(String formTitle, String dataRootType) {
	Form f = new Form(formTitle, dataRootType);
	f.props.put(PROP_DIALOG_TYPE, DialogType.stdDialog);
	Group root = (Group) f.props.get(PROP_ROOT_GROUP);
	new Group(root, new Label(Group.STD_IO_CONTROLS, null), null, null,
		null);
	new Group(root, new Label(Group.STD_SUBMITS, null), null, null, null);
	new Group(root, new Label(Group.STD_STD_BUTTONS, null), null, null,
		null);
	return f;
    }

    /**
     * Constructs and returns a new form object representing a
     * {@link DialogType#message Message} dialog that is ready to publish within
     * an {@link org.universAAL.middleware.ui.UIRequest}. See also the
     * discussion of dialog types in the above documentation of this class.
     * 
     * @param formTitle
     *            The form title giving the intent of the dialog.
     * @param message
     *            The text of the message.
     * @return A newly constructed form object representing a
     *         {@link DialogType#message Message} dialog that is ready to
     *         publish within an {@link org.universAAL.middleware.ui.UIRequest}.
     */
    public static Form newMessage(String formTitle, String message) {
	Form f = new Form(formTitle, (Resource) null);
	f.props.put(PROP_DIALOG_TYPE, DialogType.message);
	Group root = (Group) f.props.get(PROP_ROOT_GROUP);
	Group ctrls = new Group(root, new Label(Group.STD_IO_CONTROLS, null),
		null, null, null);
	new SimpleOutput(ctrls, null, null, message);
	Group submits = new Group(root, new Label(Group.STD_SUBMITS, null),
		null, null, null);
	new Submit(submits, LABEL_MESSAGE_DELET, ACK_MESSAGE_DELET);
	new Submit(submits, LABEL_MESSAGE_KEEP, ACK_MESSAGE_KEEP);
	return f;
    }

    /**
     * Constructs and returns a new form object representing an empty dialog of
     * type {@link DialogType#subdialog Subdialog} with proper initial
     * configuration. See also the discussion of dialog types in the above
     * documentation of this class. To set hidden form data, you must first get
     * the data root using {@link #getData()} and then add your data calling its
     * method
     * {@link org.universAAL.middleware.rdf.Resource#setPropertyPath(String[], Object)}
     * .
     * 
     * @param formTitle
     *            The form title giving the intent of the dialog.
     * @param parentDialogURI
     *            The ID of the parent dialog. See also
     *            {@link #PROP_PARENT_DIALOG_URI}.
     * @return A newly constructed form object representing an empty dialog of
     *         type {@link DialogType#subdialog Subdialog} with proper initial
     *         configuration.
     */
    public static Form newSubdialog(String formTitle, String parentDialogURI) {
	Form f = new Form(formTitle, (Resource) null);
	f.props.put(PROP_DIALOG_TYPE, DialogType.subdialog);
	f.props.put(PROP_PARENT_DIALOG_URI, new Resource(parentDialogURI));
	Group root = (Group) f.props.get(PROP_ROOT_GROUP);
	new Group(root, new Label(Group.STD_IO_CONTROLS, null), null, null,
		null);
	new Group(root, new Label(Group.STD_SUBMITS, null), null, null, null);
	return f;
    }

    public static Form newSystemMenu(String formTitle) {
	Form f = new Form(formTitle, (Resource) null);
	f.props.put(PROP_DIALOG_TYPE, DialogType.sysMenu);
	Group root = (Group) f.props.get(PROP_ROOT_GROUP);
	new Group(root, new Label(Group.STD_IO_CONTROLS, null), null, null,
		null);
	new Group(root, new Label(Group.STD_STD_BUTTONS, null), null, null,
		null);
	return f;
    }

    static boolean setValue(Resource pr, String[] pp, Object value,
	    MergedRestriction valueRestrictions) {
	if (pp == null || pp.length == 0)
	    return false;

	if (value instanceof List && ((List) value).isEmpty())
	    value = null;
	else if (value != null && valueRestrictions != null) {
	    Resource dummy = new Resource();
	    dummy.setProperty(pp[pp.length - 1], value);
	    if (!valueRestrictions.hasMember(dummy))
		return false;
	}

	return pr.setPropertyPathFromOffset(pp, 0, value, true);
    }

    /**
     * For usage by de-serializers only.
     */
    public Form(String uri) {
	super(uri);
	addType(MY_URI, true);
    }

    /**
     * @param uriPrefix
     * @param numProps
     */
    protected Form(String uriPrefix, int numProps) {
	super(uriPrefix, numProps);
    }

    private Form(String formTitle, Resource dataRoot) {
	super(uAAL_DIALOG_NAMESPACE, 5);
	addType(MY_URI, true);
	props.put(PROP_DIALOG_CREATION_TIME, TypeMapper.getCurrentDateTime());
	props.put(PROP_ROOT_GROUP, new Group(formTitle, this));
	props.put(PROP_DIALOG_DATA_ROOT, (dataRoot == null) ? new Resource()
		: dataRoot);
    }

    private Form(String formTitle, String dataRootType) {
	super(uAAL_DIALOG_NAMESPACE, 5);
	addType(MY_URI, true);
	props.put(PROP_DIALOG_CREATION_TIME, TypeMapper.getCurrentDateTime());
	props.put(PROP_ROOT_GROUP, new Group(formTitle, this));
	Resource root = ManagedIndividual.getInstance(dataRootType, null);
	if (root == null)
	    root = new Resource();
	props.put(PROP_DIALOG_DATA_ROOT, root);
    }

    void finalizeGroupStructure() {
	FormControl[] children = getRootGroup().getChildren();
	if (children != null)
	    for (int i = 0; i < children.length; i++)
		if (children[i] instanceof Group)
		    ((Group) children[i]).setStructuralProps(0);
    }

    /**
     * Returns the time at which the first time the form was created by an
     * application.
     * 
     * @see #PROP_DIALOG_CREATION_TIME
     */
    public XMLGregorianCalendar getCreationTime() {
	return (XMLGregorianCalendar) props.get(PROP_DIALOG_CREATION_TIME);
    }

    /**
     * @see #PROP_DIALOG_CURRENT_FOCUSED_CONTROL
     */
    public FormControl getCurrentFocusedControl() {
	return (FormControl) props.get(PROP_DIALOG_CURRENT_FOCUSED_CONTROL);
    }

    /**
     * @see #PROP_DIALOG_DATA_ROOT
     */
    public Resource getData() {
	return (Resource) props.get(PROP_DIALOG_DATA_ROOT);
    }

    /**
     * @see #PROP_DIALOG_CREATED_BY
     */
    public String getDialogCreator() {
	return (String) props.get(PROP_DIALOG_CREATED_BY);
    }

    /**
     * Returns the URI of this form object as its global unique ID.
     */
    public String getDialogID() {
	return uri;
    }

    /**
     * @see #PROP_DIALOG_TYPE
     */
    public DialogType getDialogType() {
	return (DialogType) props.get(PROP_DIALOG_TYPE);
    }

    // private int getIndex(String index) {
    // try { return Integer.parseInt(index); } catch (Exception e) { return -1;
    // }
    // }

    /**
     * Returns the standard group for UI controls in this form. See also the
     * above documentation of this class concerning the standard groups within
     * forms.
     */
    public Group getIOControls() {
	FormControl[] children = getRootGroup().getChildren();
	for (int i = 0; i < children.length; i++)
	    if (children[i] instanceof Group
		    && Group.STD_IO_CONTROLS.equals(children[i].getLabel()
			    .getText()))
		return (Group) children[i];
	return null;
    }

    /**
     * Returns the text message originally set if this form was created by
     * {@link #newMessage(String, String)}. Otherwise, it returns null.
     */
    public String getMessageContent() {
	if (isMessage()) {
	    Group main = getIOControls();
	    try {
		return (String) ((SimpleOutput) main.getChildren()[0])
			.getContent();
	    } catch (Exception e) {
	    }
	}
	return null;
    }

    /**
     * Returns the parent dialog as an empty resource with
     * {@link #PROP_PARENT_DIALOG_URI} as its URI.
     */
    public Resource getParentDialogResource() {
	return isSubdialog() ? (Resource) props.get(PROP_PARENT_DIALOG_URI)
		: null;
    }

    /**
     * @see #PROP_PARENT_DIALOG_URI
     */
    public String getParentDialogURI() {
	return isSubdialog() ? props.get(PROP_PARENT_DIALOG_URI).toString()
		: null;
    }

    MergedRestriction getPPathRestriction(String[] pp) {
	return getPPathRestriction(pp, getData());
    }

    Group getRootGroup() {
	return (Group) props.get(PROP_ROOT_GROUP);
    }

    /**
     * Returns all the {@link Output} controls contained in the ioControls group
     * (see the above documentation of this class concerning the standard groups
     * within forms) that are likely to be relevant for human users in order to
     * decide what to do with this dialog. The assumption is that there may be
     * other {@link Output} controls specific to certain submissions possible
     * within this dialog, which will be ignored by this method as they do not
     * represent shared info.
     */
    public Output[] getSharedOutputs() {
	return getIOControls().getFirstLevelOutputs();
    }

    /**
     * Returns the pre-defined group of standard buttons in this form. See also
     * the above documentation of this class concerning the standard groups
     * within forms.
     */
    public Group getStandardButtons() {
	FormControl[] children = getRootGroup().getChildren();
	for (int i = 0; i < children.length; i++)
	    if (children[i] instanceof Group
		    && Group.STD_STD_BUTTONS.equals(children[i].getLabel()
			    .getText()))
		return (Group) children[i];
	return null;
    }

    /**
     * Reserved for use by a dialog management solution that has access to all
     * dialogs and adds standard buttons beyond the application logic to reflect
     * a system-wide behavior.
     */
    public String getStandardButtonsDialogID() {
	return uri + STD_BUTTONS_DIALOG_ID_SUFFIX;
    }

    /**
     * Returns the standard group for submit buttons in this form. See also the
     * above documentation of this class concerning the standard groups within
     * forms.
     */
    public Group getSubmits() {
	FormControl[] children = getRootGroup().getChildren();
	for (int i = 0; i < children.length; i++)
	    if (children[i] instanceof Group
		    && Group.STD_SUBMITS.equals(children[i].getLabel()
			    .getText()))
		return (Group) children[i];
	return null;
    }

    /**
     * Returns the form title reflecting the intent of this dialog and
     * originally set when constructing this form object.
     */
    public String getTitle() {
	try {
	    return getRootGroup().getLabel().getText();
	} catch (NullPointerException npe) {
	    return null;
	}
    }

    String getTypeURI(String[] pp) {
	MergedRestriction r = getPPathRestriction(pp);
	return (r == null) ? null : r.getPropTypeURI();
    }

    Object getValue(String[] pp) {
	return getValue(pp, getData());
    }

    /**
     * Answers if this form object is created by
     * {@link #newDialog(String, Resource)}.
     * 
     * @see #PROP_DIALOG_TYPE
     */
    public boolean isStandardDialog() {
	return DialogType.stdDialog.equals(props.get(PROP_DIALOG_TYPE));
    }

    /**
     * Answers if this form object is created by
     * {@link #newMessage(String, String)}.
     * 
     * @see #PROP_DIALOG_TYPE
     */
    public boolean isMessage() {
	return DialogType.message.equals(props.get(PROP_DIALOG_TYPE));
    }

    /**
     * Answers if this form object is created by
     * {@link #newSubdialog(String, String)}.
     * 
     * @see #PROP_DIALOG_TYPE
     */
    public boolean isSubdialog() {
	return DialogType.subdialog.equals(props.get(PROP_DIALOG_TYPE));
    }

    /**
     * Answers if this form object is created by {@link #newSystemMenu(String)}.
     * 
     * @see #PROP_DIALOG_TYPE
     */
    public boolean isSystemMenu() {
	return DialogType.sysMenu.equals(props.get(PROP_DIALOG_TYPE));
    }

    /**
     * @see #PROP_DIALOG_CURRENT_FOCUSED_CONTROL
     */
    public void setCurrentFocusedControl(FormControl fc) {
	if (fc != null)
	    props.put(PROP_DIALOG_CURRENT_FOCUSED_CONTROL, fc);
	else
	    props.remove(PROP_DIALOG_CURRENT_FOCUSED_CONTROL);
    }

    /**
     * @see #PROP_DIALOG_CREATED_BY
     */
    public void setDialogCreator(String creator) {
	if (creator != null)
	    props.put(PROP_DIALOG_CREATED_BY, creator);
    }

    boolean setValue(String[] pp, Object value,
	    MergedRestriction valueRestrictions) {
	return setValue(getData(), pp, value, valueRestrictions);
    }

    /**
     * Reserved for usage by the middleware.
     */
    public void substituteData(Resource pr) {
	Resource cur = getData();
	if (pr != null && pr != cur) {
	    String t0 = cur.getType(), t1 = pr.getType();
	    if (t0 == t1 || (t0 != null && t0.equals(t1)))
		props.put(PROP_DIALOG_DATA_ROOT, pr);
	}
    }

    /**
     * look for a FormControl within the form with the given URI.
     * 
     * @param formControlURI
     * @return the {@link FormControl} or null if not found.
     */
    public FormControl searchFormControl(String formControlURI) {
	FormControl[] children = getRootGroup().getChildren();
	boolean found = false;
	int i = 0;
	FormControl result = null;
	while (!found && i < children.length) {
	    found = children[i].getURI().equals(formControlURI);
	    if (found) {
		result = children[i];
	    } else if (children[i] instanceof Group) {
		result = ((Group) children[i])
			.searchFormControl(formControlURI);
		found = (result != null);
	    }
	    i++;
	}
	return result;
    }
}