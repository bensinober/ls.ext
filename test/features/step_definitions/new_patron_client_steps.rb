require 'cgi'

When(/^at jeg er i søkegrensesnittet$/) do
  @site.SearchPatronClient.visit
end

When(/^jeg søker på "([^"]*)" \(\+ id på vilkårlig migrering\)$/) do |query|
  @site.SearchPatronClient.search_with_text "#{query}#{@context[:random_migrate_id]}"
end

When(/^skal jeg få "([^"]*)" treff$/) do |hits|
  wait_retry { @site.SearchPatronClient.total_hits.eql? hits }
end

When(/^jeg går til side "([^"]*)" i resultatlisten$/) do |page|
  @site.SearchPatronClient.goto_search_result_page page
end

When(/^skal jeg ha "([^"]*)" resultater og være på side "([^"]*)"$/) do |hits, page|
  wait_retry { @browser.element(class: 'pagination').a(text: page).parent.class_name.eql? 'active' }
  wait_retry { @browser.element(data_automation_id: 'hits-total').exists? }
  wait_retry { @browser.div(data_automation_id: 'search-result-entries').elements(:xpath, '*').count.to_s.eql? hits }
end

When(/^jeg trykker tilbake i nettleseren$/) do
  sleep 1 # Avoids snags in PhantomJS
  @browser.back
end

When(/^jeg trykker fremover i nettleseren$/) do
  sleep 1 # Avoids snags in PhantomJS
  @browser.forward
end

When(/^jeg trykker oppfrisk i nettleseren$/) do
  @browser.refresh
end

When(/^skal jeg se filtre på format, språk og målgruppe$/) do
  wait_retry { @browser.element(data_automation_id: 'filter_format').exists? }
  wait_retry { @browser.element(data_automation_id: 'filter_language').exists? }
  wait_retry { @browser.element(data_automation_id: 'filter_audience').exists? }
end

When(/^jeg slår på et filter for et vilkårlig format$/) do
  @browser.element(data_automation_id: 'filter_format').checkboxes.to_a.select { |checkbox| not checkbox.set? }.sample.set
end

When(/^skal jeg kun se treff med valgte format tilgjengelig$/) do
  filter_values = @browser.element(data_automation_id: 'filter_format').lis
                      .select { |li| li.checkbox.present? && li.checkbox.set? }
                      .map { |li| li.element(data_automation_id: 'filter_label').text }
  wait_retry {
    match = false
    @site.SearchPatronClient.get_search_result_list.each do |element|
      filter_values.each do |filter_value|
        if element.attribute_value('data-formats').include? filter_value
          match = true
        end
      end
    end
    match
  }
end

When(/^skal filterne være valgt i grensesnittet$/) do
  filters = CGI::parse(URI(@browser.url).query).select { |param| param.start_with? 'filter_' }
  data_automation_ids = filters.map { |aggregation, buckets| buckets.map { |bucket| "#{aggregation}_#{bucket}" } }.flatten
  filters.each do |filter, _|
    element = @browser.element(data_automation_id: filter)
    element.lis.select do |filter|
      if data_automation_ids.include? filter.attribute_value(:data_automation_id)
        filter.checkbox.set?.should eq true
      end
    end
  end
end

When(/^nåværende søketerm skal være "([^"]*)" \(\+ id på vilkårlig migrering\)$/) do |query|
  @site.SearchPatronClient.search_term.eql?("#{query}#{@context[:random_migrate_id]}")
end

When(/^skal tittel prefikset "([^"]*)" og som inneholder "([^"]*)" vises i søkeresultatet$/) do |prefix, str|
  wait_retry {
    element = @browser.element(data_automation_id: 'work-title')
    element.present? && element.text.eql?("#{prefix}#{@context[:random_migrate_id]} #{@context[:random_migrate_id]}#{str}")
  }
end

When(/^skal språket "([^"]*)" være valgt$/) do |language|
  @site.PatronClientCommon.current_language?(language).should eq true
end

When(/^søkeknappen skal vise ordet "([^"]*)"$/) do |button_text|
  wait_retry { @browser.element(data_automation_id: 'search_button').text.eql? button_text }
end

When(/^jeg søker på "([^"]*)"$/) do |query|
  @site.SearchPatronClient.search_with_text "#{query}"
end

When(/^jeg trykker på første treff$/) do
  @site.SearchPatronClient.follow_first_item_in_search_result
end

When(/^skal jeg se "([^"]*)" utgivelser$/) do |count|
  wait_retry { @site.PatronClientWorkPage.publication_entries.size.to_s.eql? count }
end

When(/^skal jeg se et panel med informasjon om utgivelsen$/) do
  wait_retry { @browser.elements(data_automation_id: /^publication_info_/).size.eql? 1 } #There should only be one
end

When(/^jeg trykker på utgivelsen med "([^"]*)" språk$/) do |language|
  @browser.elements(data_automation_id: 'publication_languages').select { |element| element.text.include? language }[0].click
end

When(/^den skal inneholde eksemplarinformasjonen$/) do |table|
  table = table.hashes.sort_by { |r| r.to_s }
  table.each do |row|
    if row['Filial'].eql? 'random_migrate_branchcode'
      row['Filial'] = @context[:random_migrate_branchcode]
    end
  end
  wait_retry { @browser.element(data_automation_id: /^publication_info_/).present? }
  publication_info = @browser.element(data_automation_id: /^publication_info_/).table.hashes.sort_by { |r| r.to_s }
  table.should eq publication_info
end

When(/^skal jeg ikke se et panel med informasjon om utgivelsen$/) do
  wait_retry { @browser.elements(data_automation_id: /^publication_info_/).size.eql? 0 }
end

When(/^skal skal tittel prefikset "([^"]*)" og som inneholder "([^"]*)" vises på verkssiden$/) do |prefix, str|
  @site.PatronClientWorkPage.title.eql?("#{prefix}#{@context[:random_migrate_id]} #{@context[:random_migrate_id]}#{str}")
end

When(/^jeg trykker på krysset i boksen med utgivelsesinformasjon$/) do
  @browser.element(data_automation_id: /^close_publication_info_/).click
end

When(/^jeg går til Min Side$/) do
  @site.PatronClientProfilePage.visit
end

When(/^skal jeg se innloggingsvinduet$/) do
  @site.PatronClientCommon.login_modal_visible?.should eq true
end

When(/^jeg logger inn$/) do
  @site.PatronClientCommon.login(@active[:patron].userid, @active[:patron].password)
end

When(/^skal jeg se informasjonen min$/) do
  @site.PatronClientProfilePage.borrowernumber.should eq @active[:patron].borrowernumber
end

When(/^låneren trykker bestill på en utgivelse$/) do
  @site.PatronClientWorkPage.click_first_reserve
end

When(/^skal jeg se reservasjonsvinduet$/) do
  wait_for { @site.PatronClientCommon.reservation_modal_visible? }
end

When(/^jeg trykker på bestill$/) do
  @browser.element(data_automation_id: 'reserve_button').click
end

When(/^får låneren tilbakemelding om at boka er reservert$/) do
  wait_for { @site.PatronClientCommon.reservation_success_modal_visible? }
end

When(/^jeg trykker for å bytte språk$/) do
  @site.PatronClientCommon.click_change_language
end

When(/^jeg velger riktig avdeling$/) do
  # Temporary hack to set the branch code to the test branch
  # Will be fixed when Patron Client can retrieve a list of existing branches from Koha
  @browser.execute_script("document.querySelector('[data-automation-id=\"reservation_modal\"]').getElementsByTagName('option')[0].setAttribute('value', \"#{@context[:random_migrate_branchcode]}\")")
end