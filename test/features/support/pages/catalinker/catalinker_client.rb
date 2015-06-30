# encoding: UTF-8

require_relative '../page_root.rb'

class CatalinkerClient < PageRoot
    def visit
      @browser.goto catalinker_client(:home)
      self
    end

    def add(title, author, date, biblio)
      @browser.button(:data_automation_id => /new_work_button/).click

      addTriple("navn", title)
      addTriple("dato", date)
      addTriple("biblio", biblio) if biblio
      addTriple("skaper", author)

      self
    end

    def addTriple(field, value)
      predicate_selector = @browser.element(:data_automation_id => /predicate_selector/)
      predicate_selector.wait_until_present
      predicate_selector.click
      predicate_selector.send_keys field, :tab, :enter

      input = @browser.text_field(:data_automation_id => field)
      input.set(value)
      input.send_keys :tab, :tab
    end

    def get_id()
      w = @browser.h2(:data_automation_id => "work_id").text
    end
end
