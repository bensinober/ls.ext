# encoding: UTF-8

Then(/^kommer jeg til verks\-siden for det aktuelle verket$/) do
  Watir::Wait.until { @browser.execute_script("return document.readyState") == "complete" }
  @site.PatronClient.getTitle.should include(@context[:title])
end


When(/^jeg er på sida til verket$/) do
  identifier = @context[:identifier].sub(services(:work).to_s + "/","")
  @site.PatronClient.visit(identifier)
end

Then(/^ordet "(.*?)" som førsteutgave vises IKKE på verks\-siden$/) do |arg1|
  step "jeg er på sida til verket"
  @site.PatronClient.getDate().should_not include(@context[:year])
end

Then(/^verkets årstall førsteutgave av vises på verks\-siden$/) do
  step "jeg er på sida til verket"
  @browser.refresh
  @site.PatronClient.getDate().should eq(@context[:year])
end


Then(/^språket til verkets tittel vises på verks\-siden$/) do
  step "jeg er på sida til verket"
  @browser.refresh
  @site.PatronClient.getTitle.should include("@" + @context[:title_lang])
end

Then(/^ser jeg informasjon om verkets tittel og utgivelsesår$/) do
  @browser.refresh
  @site.PatronClient.getTitle.should include(@context[:title])
  @site.PatronClient.getDate.should include(@context[:year])
end

Then(/^verkets tittel vises på verks\-siden$/) do
  step "jeg er på sida til verket"
  @site.PatronClient.getTitle.should include(@context[:title])
end

Then(/^verkets alternative tittel vises på verks\-siden$/) do
  step "jeg er på sida til verket"
  @browser.refresh
  @site.PatronClient.getTitle.should include(@context[:alt_title])
end

Then(/^vises eksemplaret på verkssiden$/) do
  page = @site.PatronClient.visit(@context[:identifier].split("/").last)
  page.existsExemplar.should be(true)
end

When(/^vises opplysningene om utgivelsen på verkssiden$/) do
  sleep 4 # TODO why does it take so long for previous step (knytte utgivelse til verket) to be persisted in DB?
  step "jeg er på sida til verket"
  @site.PatronClient.getPublicationsTableRows().each do |row|
    row.td(:data_automation_id => "publication_name").text.should eq(@context[:publication_name])
    row.td(:data_automation_id => "publication_format").text.should eq(@context[:publication_format])
    row.td(:data_automation_id => "publication_language").text.should eq(@context[:publication_language])
  end
end

Then(/^ser jeg en liste over eksemplarer knyttet til verket$/) do
  @browser.refresh
  @site.PatronClient.existsExemplar().should == true
end

Then(/^har eksemplarene en identifikator \(strekkode\)$/) do
  @site.PatronClient.getItemsTableRows().each do |row|
    row.td(:data_automation_id => "item_barcode").text.should_not be_empty
  end
end

Then(/^eksemplarene er gruppert etter utgave m\/informasjon om format og språk$/) do
  @site.PatronClient.getPublicationsTableRows().each do |row|
    row.td(:data_automation_id => "publication_name").text.should_not be_empty
    row.td(:data_automation_id => "publication_language").text.should_not be_empty
    row.td(:data_automation_id => "publication_format").text.should_not be_empty
  end

  @site.PatronClient.getItemsTableRows().each do |row|
    row.td(:data_automation_id => "item_title").text.should_not be_empty
    row.td(:data_automation_id => "item_language").text.should_not be_empty
    row.td(:data_automation_id => "item_format").text.should_not be_empty
    row.td(:data_automation_id => "item_barcode").text.should_not be_empty
    row.td(:data_automation_id => "item_location").text.should_not be_empty
    row.td(:data_automation_id => "item_status").text.should_not be_empty
  end
end

Then(/^ser jeg format og språk for utgivelsen$/) do
  step "vises opplysningene om utgivelsen på verkssiden"
end

When(/^ser jeg eksemplarene gruppert etter utgave m\/informasjon om format og språk$/) do
  pending # Write code here that turns the phrase above into concrete actions
end

Then(/^ser jeg lokasjon og oppstillinga av eksemplaret$/) do
  @site.PatronClient.getItemsTableRows().each do |row|
    row.td(:data_automation_id => "item_location").text.should_not be_empty
    row.td(:data_automation_id => "item_shelfmark").text.should_not be_empty
  end
end

Så(/^ser jeg at eksemplaret er ledig$/) do
  @site.PatronClient.getItemsTableRows().each do |row|
    row.td(:data_automation_id => "item_status").text.should eq("ledig")
  end
end

Så(/^ser jeg at eksemplaret ikke er ledig$/) do
  pending # Write code here that turns the phrase above into concrete actions
end
