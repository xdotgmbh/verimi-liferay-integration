package de.xdot.tlrz.openid.connect.internal;

import com.liferay.petra.string.StringBundler;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.UserEmailAddressException;
import com.liferay.portal.kernel.model.Company;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.service.CompanyLocalServiceUtil;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.UserLocalServiceUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.security.sso.openid.connect.OpenIdConnectServiceException;
import com.liferay.portal.security.sso.openid.connect.internal.OpenIdConnectUserInfoProcessor;
import com.nimbusds.openid.connect.sdk.claims.UserInfo;
import de.xdot.tlrz.openid.connect.internal.exception.StrangersNotAllowedException;
import org.osgi.service.component.annotations.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Locale;

@Component(
    immediate = true,
    property = {
            "service.ranking:Integer=100"
    },
    service = OpenIdConnectUserInfoProcessor.class
)
public class OpenIdConnectUserInfoProcessorImpl implements OpenIdConnectUserInfoProcessor {

    public long processUserInfo(UserInfo userInfo, long companyId)
		throws PortalException {

		String emailAddress = userInfo.getEmailAddress();

		User user = UserLocalServiceUtil.fetchUserByEmailAddress(
			companyId, emailAddress);

		if (user != null) {
			return user.getUserId();
		}

		checkAddUser(companyId, emailAddress);

		String firstName = userInfo.getGivenName();
		String lastName = userInfo.getFamilyName();
		String middleName = userInfo.getMiddleName();

		if (Validator.isNull(firstName) || Validator.isNull(lastName)) {
		    String fullName = userInfo.getName();
		    if (Validator.isNotNull(fullName)) {
		        int index = fullName.lastIndexOf(' ');
		        if (index > -1) {
		            firstName = fullName.substring(0, index);
		            lastName = fullName.substring(index + 1);
                }

            }

        }

        int birthdayMonth = Calendar.JANUARY;
        int birthdayDay = 1;
        int birthdayYear = 1970;

		String birthdate = userInfo.getBirthdate();
		if (Validator.isNotNull(birthdate)) {
            LocalDate birthdateDate = LocalDate.parse(birthdate, DateTimeFormatter.ofPattern("yyyy-MM-dd"));

            birthdayYear = birthdateDate.getYear();
            birthdayMonth = birthdateDate.getMonthValue() - 1;
            birthdayDay = birthdateDate.getDayOfMonth();
        }

        return createUser(companyId, emailAddress, firstName, lastName, middleName, birthdayYear, birthdayMonth, birthdayDay);
    }

    private long createUser(long companyId, String emailAddress, String firstName, String lastName, String middleName, int birthdayYear, int birthdayMonth, int birthdayDay) throws PortalException {
        User user;
        if (Validator.isNull(firstName) || Validator.isNull(lastName) ||
            Validator.isNull(emailAddress)) {

            StringBundler sb = new StringBundler(9);

            sb.append("Unable to map OpenId Connect user to the portal, ");
            sb.append("missing or invalid profile information: ");
            sb.append("{emailAddresss=");
            sb.append(emailAddress);
            sb.append(", firstName=");
            sb.append(firstName);
            sb.append(", lastName=");
            sb.append(lastName);
            sb.append("}");

            throw new OpenIdConnectServiceException.UserMappingException(
                sb.toString());
        }

        long creatorUserId = 0;
        boolean autoPassword = true;
        String password1 = null;
        String password2 = null;
        boolean autoScreenName = true;
        String screenName = StringPool.BLANK;
        long facebookId = 0;

        Company company = CompanyLocalServiceUtil.getCompany(companyId);

        Locale locale = company.getLocale();

        long prefixId = 0;
        long suffixId = 0;
        boolean male = true;
        String jobTitle = StringPool.BLANK;
        long[] groupIds = null;
        long[] organizationIds = null;
        long[] roleIds = null;
        long[] userGroupIds = null;
        boolean sendEmail = false;

        ServiceContext serviceContext = new ServiceContext();

        user = UserLocalServiceUtil.addUser(
            creatorUserId, companyId, autoPassword, password1, password2,
            autoScreenName, screenName, emailAddress, facebookId, null, locale,
            firstName, middleName, lastName, prefixId, suffixId, male,
            birthdayMonth, birthdayDay, birthdayYear, jobTitle, groupIds,
            organizationIds, roleIds, userGroupIds, sendEmail, serviceContext);

        user = UserLocalServiceUtil.updatePasswordReset(user.getUserId(), false);

        return user.getUserId();
    }

    protected void checkAddUser(long companyId, String emailAddress)
		throws PortalException {

		Company company = CompanyLocalServiceUtil.getCompany(companyId);

		if (!company.isStrangers()) {
			throw new StrangersNotAllowedException(companyId);
		}

		if (!company.isStrangersWithMx() &&
			company.hasCompanyMx(emailAddress)) {

			throw new UserEmailAddressException.MustNotUseCompanyMx(
				emailAddress);
		}
	}

}