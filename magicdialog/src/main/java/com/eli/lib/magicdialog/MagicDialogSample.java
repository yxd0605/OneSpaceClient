package com.eli.lib.magicdialog;

import android.app.Activity;
import android.graphics.Color;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * Created by Administrator on 2016/4/21.
 */
public class MagicDialogSample {

    public static void notice(final Activity activity) {
        MagicDialog dialog = new MagicDialog(activity);
        dialog.title("Notify Title").notice().content("Notify Content Body.").positive("OK").bold(null).warning().check("Show List Dialog").checked(true)
                .listener(new OnMagicDialogClickCallback() {
                    @Override
                    public void onClick(View view, MagicDialog.MagicDialogButton button, boolean checked) {
                        if (checked) {
                            MagicDialogSample.list(activity);
                        }
                    }
                }).show();
    }

    public static void confirm(final Activity activity) {
        MagicDialog dialog = new MagicDialog(activity);
        dialog.title("Confirm Title").confirm().content("Confirm Content Body").warning().positive("OK").negative("Cancel")
                .check("Show Notice Dialog?").checked(true).bold(MagicDialog.MagicDialogButton.POSITIVE).right(MagicDialog.MagicDialogButton.POSITIVE)
                .listener(new OnMagicDialogClickCallback() {
                    @Override
                    public void onClick(View view, MagicDialog.MagicDialogButton button, boolean checked) {
                        if (button == MagicDialog.MagicDialogButton.POSITIVE && checked) {
                            MagicDialogSample.notice(activity);
                        }
                    }
                }).show();
    }

    public static void edit(final Activity activity) {
        MagicDialog dialog = new MagicDialog(activity);
        dialog.title("EditDialog Title").content("Confirm Content Body").hint("Please enter your name").unit("MB").warning().positive("OK").negative("Verify")
                .check("Show Notice Dialog?").checked(true).bold(MagicDialog.MagicDialogButton.POSITIVE).right(MagicDialog.MagicDialogButton.POSITIVE)
                .listener(new OnMagicDialogClickCallback() {
                    @Override
                    public boolean onClick(View view, MagicDialog.MagicDialogButton button, EditText editText, boolean checked) {
                        String input = editText.getText().toString();
                        if (button == MagicDialog.MagicDialogButton.POSITIVE) {
                            if (EmptyUtils.isEmpty(input)) {
                                Toast.makeText(activity, "Please enter your name!!!", Toast.LENGTH_SHORT).show();
                                return false;
                            } else {
                                if (checked) {
                                    MagicDialogSample.notice(activity);
                                }
                                return true;
                            }
                        } else {
                            MagicDialogSample.verify(activity);
                        }

                        return true;
                    }
                }).show();
    }

    public static void verify(final Activity activity) {
        MagicDialog dialog = new MagicDialog(activity);
        dialog.title("VerifyDialog Title").content("Please remember your password").hint("Please enter your password").verify("Please confirm password")
                .warning().positive("Verify").negative("Cancel").check("Show Notice Dialog?").checked(true).bold(MagicDialog.MagicDialogButton.POSITIVE).right(MagicDialog.MagicDialogButton.POSITIVE)
                .listener(new OnMagicDialogClickCallback() {
                    @Override
                    public boolean onClick(View view, MagicDialog.MagicDialogButton button, EditText editText, EditText verifyEditText, boolean checked) {
                        String input = editText.getText().toString();
                        String verify = verifyEditText.getText().toString();
                        if (button == MagicDialog.MagicDialogButton.POSITIVE) {
                            if (EmptyUtils.isEmpty(input)) {
                                Toast.makeText(activity, "Please enter your password!!!", Toast.LENGTH_SHORT).show();
                                return false;
                            } else if (EmptyUtils.isEmpty(verify)) {
                                Toast.makeText(activity, "Please confirm your password!!!", Toast.LENGTH_SHORT).show();
                                return false;
                            } else {
                                if (!input.equals(verify)) {
                                    Toast.makeText(activity, "Confirm password Failed!!!", Toast.LENGTH_SHORT).show();
                                    return false;
                                } else {
                                    Toast.makeText(activity, "Confirm password Succeed!!!", Toast.LENGTH_SHORT).show();
                                    MagicDialogSample.notice(activity);
                                }
                                return true;
                            }
                        }

                        return true;
                    }
                }).show();
    }

    public static void list(final Activity activity) {
        ArrayList<MagicDialog.MagicDialogListItem> itemList = new ArrayList<>();
        MagicDialog.MagicDialogListItem item3 = new MagicDialog.MagicDialogListItem();
        item3.title = "List:";
        item3.content = "Magic List Dialog";
        item3.color = Color.BLACK;
        itemList.add(item3);
        MagicDialog.MagicDialogListItem item2 = new MagicDialog.MagicDialogListItem();
        item2.title = "Confirm:";
        item2.content = "Magic Confirm Dialog";
        item2.color = Color.GREEN;
        itemList.add(item2);
        MagicDialog.MagicDialogListItem item1 = new MagicDialog.MagicDialogListItem();
        item1.title = "Notice:";
        item1.content = "Magic Notify Dialog";
        item1.color = Color.RED;
        itemList.add(item1);

        MagicDialog dialog = new MagicDialog(activity);
        dialog.title("List Title").list(itemList).content("This is MagicDialog Tester").warning().positive("ConfirmDialog").negative("EditDialog").neutral("NotifyDialog")
                .bold(MagicDialog.MagicDialogButton.NEGATIVE).listener(new OnMagicDialogClickCallback() {
            @Override
            public void onClick(View view, MagicDialog.MagicDialogButton button, boolean checked) {
                if (button == MagicDialog.MagicDialogButton.POSITIVE) {
                    MagicDialogSample.confirm(activity);
                } else if (button == MagicDialog.MagicDialogButton.NEUTRAL) {
                    MagicDialogSample.notice(activity);
                } else {
                    MagicDialogSample.edit(activity);
                }
            }
        }).cancelable(true).show();
    }
}
